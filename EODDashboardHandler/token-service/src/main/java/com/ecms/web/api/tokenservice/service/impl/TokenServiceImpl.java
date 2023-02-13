package com.ecms.web.api.tokenservice.service.impl;

import com.ecms.web.api.tokenservice.model.bean.*;
import com.ecms.web.api.tokenservice.model.entity.*;
import com.ecms.web.api.tokenservice.repository.*;
import com.ecms.web.api.tokenservice.security.JWTUtil;
import com.ecms.web.api.tokenservice.service.CommonService;
import com.ecms.web.api.tokenservice.service.TokenService;
import com.ecms.web.api.tokenservice.util.*;
import com.ecms.web.api.tokenservice.validators.TokenRequestValidator;
import io.jsonwebtoken.Claims;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;

import java.nio.file.AccessDeniedException;
import java.util.Date;
import java.util.Optional;

@Service
public class TokenServiceImpl implements TokenService {

    @Autowired
    private Environment env;
    @Autowired
    private TokenRequestValidator tokenRequestValidator;
    @Autowired
    private SystemuserRepository systemuserRepository;
    @Autowired
    private UserroleRepository userroleRepository;
    @Autowired
    private UserroletypeRepository userroletypeRepository;
    @Autowired
    private UserlevelRepository userlevelRepository;
    @Autowired
    private PasswordpolicyRepository passwordpolicyRepository;
    @Autowired
    private PasswordUtil passwordUtil;
    @Autowired
    private CommonService commonService;
    @Autowired
    private StatusRepository statusRepository;
    @Autowired
    private JWTUtil jwtUtil;

    SystemuserBean systemuserBean = null;

    PasswordpolicyBean passwordpolicyBean = null;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private CommonRepository commonRepository;

    @Autowired
    private SystemauditRepository systemauditRepository;

    String auditDescription;

    @Override
    public ResponseBean generateJWT(RequestBean requestBean, ResponseBean responseBean, String requestID) throws Exception {
        TokenRequest tokenRequest = modelMapper.map(requestBean.getRequestBody(), TokenRequest.class);
        BindingResult bindingResult = validateRequestBean(tokenRequest);
        String responsCode = ResponseCode.RSP_ERROR;
        String message = "";
        String token = "";
        Token tokenBean = null;

        if (bindingResult.hasErrors()) {
            message = bindingResult.getAllErrors().get(0).getDefaultMessage();
        } else {
            message = this.validateUser(tokenRequest);

            if (message.isEmpty()) {
                responsCode = ResponseCode.RSP_SUCCESS;
                String status = systemuserBean.getPasswordstatus();
                token = jwtUtil.generateToken(requestBean.getClient_ip(), systemuserBean);
                tokenBean = new Token();
                tokenBean.setToken(token);

                if (status.equalsIgnoreCase(StatusVarList.STATUS_NEW)) {
                    message = MessageVarList.PASSWORDRESET_NEWUSER;
                } else if (status.equalsIgnoreCase(StatusVarList.STATUS_RESET)) {
                    message = MessageVarList.PASSWORDRESET_RESETUSER;
                } else if (status.equalsIgnoreCase(StatusVarList.STATUS_EXPIRED)) {
                    message = MessageVarList.PASSWORDRESET_EXPPWD;
                } else {
                    String msg = checkPwdExpNotification(systemuserBean, passwordpolicyBean);

                    message = "Last logged date : " + Common.formatDatetoString(systemuserBean.getLastloggeddate()) + ". " + msg;

                    systemuserBean.setUpdateflag(true);
                    systemuserBean.setLastloggeddate(commonService.getSysDate());
                    systemuserBean.setInvalidloginattempt(new Byte("0"));

                    auditDescription = "Login successfully";
                }
            }

            if (systemuserBean.isUpdateflag())
                systemuserBean.setLastupdatedtime(commonService.getSysDate());
            this.updateSystemUser(systemuserBean);

            //set the audit trace values
            if (!auditDescription.isEmpty()) {
                Systemaudit systemaudit = new Systemaudit();
                systemaudit.setIp(requestBean.getClient_ip());
                systemaudit.setUsername(systemuserBean.getUsername());
                systemaudit.setUserrole(systemuserBean.getUserrole());
                systemaudit.setPage("LOGIN");
                systemaudit.setTask("LOGIN");
                systemaudit.setDescription(auditDescription);
                systemaudit.setAffectedkey(systemuserBean.getUsername());
                systemaudit.setRequestid(requestID);
                systemaudit.setCreatedtime(commonRepository.getSysDate());
                systemauditRepository.save(systemaudit);
            }

        }

        responseBean.setResponseCode(responsCode);
        responseBean.setResponseMsg(message);
        responseBean.setContent(tokenBean);

        return responseBean;
    }

    @Override
    public ResponseBean validateJWT(RequestBean requestBean, ResponseBean responseBean) throws Exception {
        String responsCode = ResponseCode.RSP_ERROR;
        String ip = "";
        ClientBean clientBean = new ClientBean();

        String jwt = jwtUtil.parseJwt(requestBean.getToken());
        if (jwt != null) {
            Claims claims = jwtUtil.getClaimsFromJwtToken(jwt);
            if (claims != null) {
                ip = (String) claims.get("client_ip");
                String userRole = (String) claims.get("userrole");
                String username = (String) claims.get("username");
                int userlevel = (int) claims.get("userlevel");

                clientBean.setUserrole(userRole);
                clientBean.setUsername(username);
                clientBean.setUserlevel(userlevel);

                if (!ip.equalsIgnoreCase(requestBean.getClient_ip())) {
                    throw new AccessDeniedException("");
                } else {
                    responsCode = ResponseCode.RSP_SUCCESS;
                }
            } else {
                throw new IllegalArgumentException();
            }
        } else {
            throw new AccessDeniedException("");
        }

        responseBean.setResponseCode(responsCode);
        responseBean.setResponseMsg(null);
        responseBean.setContent(clientBean);

        return responseBean;
    }

    private String validateUser(TokenRequest tokenRequest) throws Exception {
        String message = "";

        systemuserBean = this.findByUsername(tokenRequest.getUsername());

        if (systemuserBean == null) {
            message = MessageVarList.LOGIN_INVALID;
        } else {
            UserroleBean userroleBean = this.findByUserroleCode(systemuserBean.getUserrole());
            UserroletypeBean userroletypeBean = this.findByUserroleTypeCode(userroleBean.getUserroletype());
            UserlevelBean userlevelBean = this.findByUserLevelCode(userroleBean.getUserlevel());
            systemuserBean.setUserlevel(userlevelBean.getUserlevelcode());

            passwordpolicyBean = this.findByPasswordpolicycode(userroleBean.getUserroletype());

            if (!passwordUtil.ValidatePassword(tokenRequest.getPassword(), systemuserBean.getPassword())) {
                message = MessageVarList.LOGIN_INVALID;

                if (systemuserBean.getInvalidloginattempt() == null) {
                    systemuserBean.setInvalidloginattempt(new Byte("0"));
                }

                byte attempts = systemuserBean.getInvalidloginattempt().byteValue();
                attempts++;

                systemuserBean.setUpdateflag(true);
                systemuserBean.setInvalidloginattempt(attempts);

                auditDescription = "Login failed due to invalid attempt " + attempts;

                if (passwordpolicyBean != null && systemuserBean.getInvalidloginattempt() >= passwordpolicyBean.getNoofinvalidloginattempt()) {
                    systemuserBean.setStatus(StatusVarList.STATUS_DEACTIVE);
                    message = MessageVarList.LOGIN_DEACTIVE;
                }
            } else if (systemuserBean.getStatus().equals(StatusVarList.STATUS_DEACTIVE)) {
                message = MessageVarList.LOGIN_DEACTIVE;
                auditDescription = "Login failed due to account deactivate";
            } else if (userroleBean.getStatus().equals(StatusVarList.STATUS_DEACTIVE)) {
                message = MessageVarList.LOGIN_DEACTIVE;
                auditDescription = "Login failed due to userrole deactivate";
            } else if (userroletypeBean.getStatus().equals(StatusVarList.STATUS_DEACTIVE)) {
                message = MessageVarList.LOGIN_DEACTIVE;
                auditDescription = "Login failed due to userrole type deactivate";
            } else if (userlevelBean.getStatus().equals(StatusVarList.STATUS_DEACTIVE)) {
                message = MessageVarList.LOGIN_DEACTIVE;
                auditDescription = "Login failed due to user level deactivate";
            } else if (checkUserIncative(systemuserBean, passwordpolicyBean)) {
                systemuserBean.setUpdateflag(true);
                systemuserBean.setStatus(StatusVarList.STATUS_DEACTIVE);
                message = MessageVarList.LOGIN_IDLEDEACTIVE;
                auditDescription = "Login de-activated due to account been idle";
            } else if (!systemuserBean.getPasswordstatus().equalsIgnoreCase(StatusVarList.STATUS_EXPIRED) && checkPasswordExpire(systemuserBean)) {
                systemuserBean.setUpdateflag(true);
                systemuserBean.setPasswordstatus(StatusVarList.STATUS_EXPIRED);
                auditDescription = "Login failed due to password expiry";
            }
        }
        return message;
    }

    private SystemuserBean findByUsername(String username) {
        SystemuserBean systemuserBean = null;
        Optional<Systemuser> systemuser = systemuserRepository.findById(username);
        if (systemuser.isPresent()) {
            systemuserBean = new SystemuserBean();
            systemuserBean.setUsername(systemuser.get().getUsername());
            systemuserBean.setFullname(systemuser.get().getFullname());
            systemuserBean.setPassword(systemuser.get().getPassword());
            systemuserBean.setUserrole(systemuser.get().getUserrole().getUserrolecode());
            systemuserBean.setEmail(systemuser.get().getEmail());
            systemuserBean.setInvalidloginattempt(systemuser.get().getInvalidloginattempt());
            systemuserBean.setExpirydate(systemuser.get().getExpirydate());
            systemuserBean.setLastloggeddate(systemuser.get().getLastloggeddate());
            systemuserBean.setPasswordstatus(systemuser.get().getPasswordstatus().getStatuscode());
            systemuserBean.setStatus(systemuser.get().getStatus().getStatuscode());
            systemuserBean.setCreatedtime(systemuser.get().getStatus().getCreatedtime());
            systemuserBean.setLastupdatedtime(systemuser.get().getStatus().getLastupdatedtime());
            systemuserBean.setLastupdateduser(systemuser.get().getStatus().getStatuscode());
        }
        return systemuserBean;
    }

    private UserroleBean findByUserroleCode(String userolecode) {
        UserroleBean userroleBean = null;
        Optional<Userrole> userrole = userroleRepository.findById(userolecode);
        if (userrole.isPresent()) {
            userroleBean = new UserroleBean();
            userroleBean.setUserrolecode(userrole.get().getUserrolecode());
            userroleBean.setUserroletype(userrole.get().getUserroletype().getUserroletypecode());
            userroleBean.setUserlevel(userrole.get().getUserlevel().getUserlevelcode());
            userroleBean.setStatus(userrole.get().getStatus().getStatuscode());
        }
        return userroleBean;
    }

    private UserroletypeBean findByUserroleTypeCode(String useroletypecode) {
        UserroletypeBean userroletypeBean = null;
        Optional<Userroletype> userroletype = userroletypeRepository.findById(useroletypecode);
        if (userroletype.isPresent())
            userroletypeBean = modelMapper.map(userroletype.get(), UserroletypeBean.class);
        return userroletypeBean;
    }

    private UserlevelBean findByUserLevelCode(long userlevelcode) {
        UserlevelBean userlevelBean = null;
        Optional<Userlevel> userlevel = userlevelRepository.findById(userlevelcode);
        if (userlevel.isPresent())
            userlevelBean = modelMapper.map(userlevel.get(), UserlevelBean.class);
        return userlevelBean;
    }

    private PasswordpolicyBean findByPasswordpolicycode(String passwordPolicyCode) {
        PasswordpolicyBean passwordpolicyBean = null;
        Passwordpolicy passwordpolicy = passwordpolicyRepository.findByPasswordpolicycode(passwordPolicyCode);
        if (passwordpolicy != null)
            passwordpolicyBean = modelMapper.map(passwordpolicy, PasswordpolicyBean.class);

        return passwordpolicyBean;
    }

    private boolean checkUserIncative(SystemuserBean systemuserBean, PasswordpolicyBean passwordpolicyBean) throws Exception {
        boolean status = false;
        Date sysDate = commonService.getSysDate();

        if (passwordpolicyBean == null)
            status = false;

        long maxinactdays = passwordpolicyBean.getIdleaccountexpiryperiod();

        if (maxinactdays == 0)
            status = false;

        long maxinactime = maxinactdays * CommonVarList.TIMESTAMPVALUE_PERDAY;
        if (sysDate != null && systemuserBean.getLastloggeddate() != null && maxinactdays > 0) {
            long currincativetime = sysDate.getTime() - systemuserBean.getLastloggeddate().getTime();
            if (currincativetime >= maxinactime) {
                status = true;
            }
        }
        return status;
    }

    private boolean checkPasswordExpire(SystemuserBean systemuserBean) throws Exception {
        boolean status = false;
        Date sysDate = commonService.getSysDate();
        Date expiredate = systemuserBean.getExpirydate();
        if (expiredate != null && sysDate != null && expiredate.getTime() <= sysDate.getTime())
            status = true;
        return status;
    }

    private String checkPwdExpNotification(SystemuserBean systemuserBean, PasswordpolicyBean passwordpolicyBean) throws Exception {
        String msg = "";
        Date sysDate = commonService.getSysDate();
        int notificationdays = 0;

        if (passwordpolicyBean != null)
            notificationdays = passwordpolicyBean.getPasswordexpirynotifyperiod();

        if (notificationdays == 0) {
            notificationdays = CommonVarList.PWPARM_EXP_NOTIFICATION;
        }
        long maxinactime = notificationdays * CommonVarList.TIMESTAMPVALUE_PERDAY;
        if (sysDate != null && systemuserBean.getExpirydate() != null && notificationdays > 0) {
            long currincativetime = systemuserBean.getExpirydate().getTime() - sysDate.getTime();
            if (maxinactime >= currincativetime) {
                long diffSeconds = currincativetime / 1000 % 60;
                long diffMinutes = currincativetime / (60 * 1000) % 60;
                long diffHours = currincativetime / (60 * 60 * 1000) % 24;
                long diffDays = currincativetime / (24 * 60 * 60 * 1000);

                System.out.print(diffDays + " days, ");
                System.out.print(diffHours + " hours, ");
                System.out.print(diffMinutes + " minutes, ");
                System.out.print(diffSeconds + " seconds.");

                if (diffDays > 0) {
                    msg = diffDays + " day(s)";
                }

                if (diffHours > 0) {
                    if (msg.isEmpty()) {
                        msg = diffHours + " hour(s)";
                    } else {
                        msg = msg + " and " + diffHours + " hour(s)";
                    }

                }
                msg = "Your password will expire on " + msg;

            }
        }
        return msg;
    }

    private boolean updateSystemUser(SystemuserBean systemuserBean) throws Exception {
        Optional<Systemuser> systemuser = systemuserRepository.findById(systemuserBean.getUsername());
        if (systemuser.isPresent()) {
            Systemuser sysUser = systemuser.get();
            sysUser.setInvalidloginattempt(systemuserBean.getInvalidloginattempt());

            Optional<Status> status = statusRepository.findById(systemuserBean.getStatus());
            if (status.isPresent())
                sysUser.setStatus(status.get());

            Optional<Status> passwordStatus = statusRepository.findById(systemuserBean.getPasswordstatus());
            if (passwordStatus.isPresent())
                sysUser.setPasswordstatus(passwordStatus.get());

            sysUser.setLastloggeddate(systemuserBean.getLastloggeddate());
            sysUser.setLastupdatedtime(systemuserBean.getLastupdatedtime());
            sysUser.setLastupdateduser(systemuserBean.getUsername());

            systemuserRepository.saveAndFlush(sysUser);

            return true;
        } else {
            return false;
        }
    }

    private BindingResult validateRequestBean(Object object) {
        DataBinder dataBinder = new DataBinder(object);
        dataBinder.setValidator(tokenRequestValidator);
        dataBinder.validate();
        return dataBinder.getBindingResult();
    }

}
