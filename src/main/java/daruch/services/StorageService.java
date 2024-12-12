package daruch.services;

import daruch.config.StorageConfig;
import daruch.services.utils.ServiceResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class StorageService {

    private final daruch.config.StorageConfig storageConfig;

    @Autowired
    public StorageService(StorageConfig storageConfigArg) {
        storageConfig = storageConfigArg;
    }

    public ServiceResult<StorageWriteResult> writeFileToStorage(MultipartFile file, String relativePath) {
        var result = new ServiceResult<StorageWriteResult>();
        var filePath = Paths.get(getMainPathStorage(), relativePath, getRandomFilename());
        var fileExtension = getFileExtension(file);
        try {
            byte[] bytesFile = file.getBytes();
            Files.write(filePath, bytesFile);
            return result.setSuccessfulAndReturn(
                    new StorageWriteResult(filePath, file.getOriginalFilename(), fileExtension)
            );
        } catch(IOException e) {
            return result.addErrorAndReturn(e.getMessage());
        }
    }

    public ServiceResult<StorageWriteResult> writeFileToStorageWithOptions(MultipartFile file, FileOptions fileOptions) {
        var result = new ServiceResult<StorageWriteResult>();
        var resultFileCheck = fileOptions.ensureFileIsOk(file);
        if (!resultFileCheck.getSuccess()) {
            return result.cloneFailureAndReturn(resultFileCheck);
        }
        return writeFileToStorage(file, fileOptions.getRelativePath());
    }

    public ServiceResult<Object> eraseFileFromStorage(String relativePath) {
        var result = new ServiceResult<>();
        var filePath = Paths.get(this.getMainPathStorage(), relativePath);
        if (!Files.exists(filePath)) {
            return result.addErrorAndReturn("File does not exists.");
        }
        try {
            Files.delete(filePath);
            return result.setSuccessfulAndReturn();
        }  catch(IOException e) {
            return result.addErrorAndReturn(e.getMessage());
        }
    }

    public ServiceResult<StorageGetResult> getFileFromStorage(String relativePath) {
        var result = new ServiceResult<StorageGetResult>();
        var filePath = Paths.get(this.getMainPathStorage(), relativePath);
        if (!Files.exists(filePath)) {
            return result.addErrorAndReturn("File does not exists.");
        }
        try {
            return result.setSuccessfulAndReturn(
                    new StorageGetResult(Files.readAllBytes(filePath))
            );
        }  catch(IOException e) {
            return result.addErrorAndReturn(e.getMessage());
        }
    }

    private String getMainPathStorage() {
        return this.storageConfig.getPath();
    }

    private static String getRandomFilename() {
        Instant now = Instant.now();
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
                .appendLiteral('Z')
                .toFormatter();
        String timestamp = formatter.format(now);
        return timestamp + "-" + UUID.randomUUID();
    }

    private static String getFileExtension(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            return null;
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex == -1) {
            return null;
        } else {
            return filename.substring(dotIndex);
        }
    }

    // Results
    public record StorageWriteResult(Path filePath, String originalFilename, String mimeType) {}
    public record StorageGetResult(byte[] fileBytes) {}

    // Options
    public static class FileOptions {
        public String getRelativePath() {
            return Paths.get(String.valueOf(getFolderPath())).toString();
        }

        public ServiceResult<Object> ensureFileIsOk(MultipartFile file) {
            var result = new ServiceResult<>();
            if (file.isEmpty()) {
                result.addError("File is empty");
            }
            if (!getPermitedExtensions().contains(getFileExtension(file))) {
                result.addError("File extension is not accepted");
            }
            long fileSize = file.getSize();
            if (fileSize > getMaxFileLength()) {
                result.addError(String.format("File is too big : %d > %d", fileSize, getMaxFileLength()));
            }
            if (!result.getErrors().isEmpty()) {
                return result;
            }
            return result.setSuccessfulAndReturn();
        }

        public List<String> getFolderPath() {
            return new ArrayList<>();
        }

        public List<String> getPermitedExtensions() {
            return new ArrayList<>();
        }

        public Long getMaxFileLength() {
            return 0L;
        }
    }
    public static class UserFileOptions extends FileOptions {
        @Override
        public List<String> getFolderPath() {
            return new ArrayList<>(List.of("User"));
        }

        @Override
        public List<String> getPermitedExtensions() {
            return  new ArrayList<>(List.of("csv"));
        }

        @Override
        public Long getMaxFileLength() {
            return 5L * 1024L * 1024L;
        }
    }
}