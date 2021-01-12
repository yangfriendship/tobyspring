package springbook.user.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

public class MockMailSender implements MailSender {

    private List<String> requests = new ArrayList<String>();

    public List<String> getRequests() {
        return requests;
    }

    @Override
    public void send(SimpleMailMessage simpleMailMessage) throws MailException {
        for (String email : simpleMailMessage.getTo()) {
            requests.add(email);
        }
    }

    @Override
    public void send(SimpleMailMessage[] simpleMailMessages) throws MailException {

    }
}
