package it.uniroma2.tutorlink.bean;

import it.uniroma2.tutorlink.exception.ValidationException;
import java.util.Arrays;

public class CredentialsBean extends AbstractBean {
    private String email;
    // char[] e non String, cosi' posso azzerarlo dopo l'uso
    private char[] password;

    public CredentialsBean() {
        super();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public char[] getPassword() {
        return password == null ? new char[0] : password.clone();
    }

    public void setPassword(char[] password) {
        this.password = password == null ? null : password.clone();
    }

    public void clearPassword() {
        if (password != null) {
            Arrays.fill(password, '\0');
        }
    }

    @Override
    public void validateSyntax() throws ValidationException {
        this.email = requireEmail(email, "email");
        if (password == null || password.length == 0) {
            throw new ValidationException("password", "the password cannot be empty");
        }
    }
}
