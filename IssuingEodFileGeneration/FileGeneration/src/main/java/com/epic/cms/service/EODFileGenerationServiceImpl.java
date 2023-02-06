/**
 * Author : lahiru_p
 * Date : 11/15/2022
 * Time : 3:56 PM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.service;

import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;

import com.itextpdf.tool.xml.XMLWorkerHelper;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.StringReader;
import java.nio.file.*;

import static com.epic.cms.util.LogManager.errorLogger;

@Service
public class EODFileGenerationServiceImpl implements FileGenerationService {

    @Override
    public void generateFile(String content, String filePath, String backUpFilePath) throws Exception {
        try {
            // Defining the file name of the file
            Path path = Paths.get(filePath);

            // Assigning the content of the file
            String text = content;

            //The method will return true when the file or directory is found and false if the file is not found.
            boolean isExistFile = Files.exists(path);


            // Writing into the file
            Files.writeString(path, text, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            /**
             * StandardOpenOption.CREATE and StandardOpenOption.APPEND.
             * If the file does not exist, the API will create and write text to the file;
             * if the file exists, append the text to the end of the file.
             */

            // Reading the content of the file
            String file_content = Files.readString(path);

            // Printing the content inside the file
            System.out.println(file_content);

            //BackUp File
            copyAndBackUpFile(filePath, backUpFilePath);

        } catch (Exception e) {
            errorLogger.error("File Writing Failed  ", e);
            throw e;
        }
    }

    @Override
    public void copyAndBackUpFile(String filePath, String backUpFilePath) throws Exception {
        try {
            // default - if backupFile exist, throws FileAlreadyExistsException
            //Files.copy(Path.of(filePath), Path.of(backUpFilePath));

            //if backupFile exist, replace it.
            Files.copy(Path.of(filePath), Path.of(backUpFilePath), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            errorLogger.error("Exception Copy File ", e);
        }
    }

    @Override
    public void createDirectoriesForFileAndBackUpFile(String filePath, String backUpFilePath) throws Exception {
        try {
            Path path1 = Paths.get(filePath);
            Path path2 = Paths.get(backUpFilePath);

            Files.createDirectories(path1);
            Files.createDirectories(path2);

        } catch (Exception e) {
            errorLogger.error("Exception in File Directory Creating ", e);
        }
    }

    @Override
    public void generatePDFFile(String content, String filePath, String backUpFilePath) throws Exception {
        try {
            content = content.replace("<br>","<br/>" );
            Document document = new Document(PageSize.LETTER);
            Document document1 = new Document(PageSize.LETTER);

            //create file
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));

            //create backUp file
            PdfWriter writer1 = PdfWriter.getInstance(document1, new FileOutputStream(backUpFilePath));

            document.open();
            document1.open();
            XMLWorkerHelper worker = XMLWorkerHelper.getInstance();
            XMLWorkerHelper worker1 = XMLWorkerHelper.getInstance();
            //InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            worker.parseXHtml(writer, document,  new StringReader(content));
            worker1.parseXHtml(writer1, document1,  new StringReader(content));
            document.close();
            document1.close();
        } catch (Exception e) {
            errorLogger.error("Exception in PDF File Generation ", e);
            throw e;
        }
    }

    @Override
    public void deleteExistFile(String filePath) throws Exception {
        try {
            // Defining the file name of the file
            Path path = Paths.get(filePath);

            //The method will return true when the file or directory is found and false if the file is not found.
            boolean isExistFile = Files.exists(path);

            if(isExistFile){
                Files.delete(path);
            }
        }catch (Exception e){
            errorLogger.error("Exception in delete exist File ", e);
        }

    }
}
