/**
 * Author :
 * Date : 2/1/2023
 * Time : 4:41 PM
 * Project Name : ecms_eod_file_processing_engine
 */

package com.epic.cms.config.policy;

import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;

public class ExceptionSkipPolicy implements SkipPolicy {
    @Override
    public boolean shouldSkip(Throwable throwable, int i) throws SkipLimitExceededException {
        return throwable instanceof Exception && i == 3;
    }
}
