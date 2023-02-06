package com.epic.cms.service;

public interface FileGenerationService {
    void generateFile(String content, String filePath, String backUpFilePath) throws Exception;

    void copyAndBackUpFile(String filePath, String backUpFilePath) throws Exception;

    void createDirectoriesForFileAndBackUpFile(String rb36FilePath, String backUpFilePath) throws Exception;

    void generatePDFFile(String content, String filePath, String backUpFilePath) throws Exception;

    void deleteExistFile(String filePath) throws Exception;
}
