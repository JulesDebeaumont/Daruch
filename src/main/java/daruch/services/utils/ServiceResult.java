package daruch.services.utils;

import java.util.ArrayList;
import java.util.List;

public class ServiceResult<T> {
    private Boolean success = false;
    private List<String> errors = new ArrayList<>();
    private T data;

    public void addError(String error) {
        errors.add(error);
    }

    public ServiceResult<T> addErrorAndReturn(String error) {
        addError(error);
        return this;
    }

    public void setSuccessful(T dataToSet) {
        success = true;
        data = dataToSet;
    }

    public void setSuccessful() {
        success = true;
    }

    public ServiceResult<T> setSuccessfulAndReturn(T dataToSet) {
        setSuccessful(dataToSet);
        return this;
    }

    public ServiceResult<T> setSuccessfulAndReturn() {
        success = true;
        return this;
    }

    public ServiceResult<T> cloneFailureAndReturn(ServiceResult<Object> resultToClone) {
        success = false;
        errors = resultToClone.errors;
        return this;
    }

    public Boolean getSuccess() {
        return success;
    }

    public List<String> getErrors() {
        return errors;
    }

    public T getData() {
        return data;
    }
}
