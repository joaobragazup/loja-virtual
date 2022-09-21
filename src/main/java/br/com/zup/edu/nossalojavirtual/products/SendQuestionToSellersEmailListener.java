package br.com.zup.edu.nossalojavirtual.products;

import br.com.zup.edu.nossalojavirtual.shared.email.Email;
import br.com.zup.edu.nossalojavirtual.shared.email.EmailRepository;
import br.com.zup.edu.nossalojavirtual.shared.email.EmailService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
class SendQuestionToSellersEmailListener {

    private final EmailService sendEmail;
    private final EmailRepository emailRepository;

    SendQuestionToSellersEmailListener(EmailService sendEmail,
                                       EmailRepository emailRepository) {
        this.sendEmail = sendEmail;
        this.emailRepository = emailRepository;
    }

    @EventListener
    void listen(QuestionEvent questionEvent) {

        var subject = " You have a new question";
        var body = questionEvent.getTitle() + " in " + questionEvent.getProductUri();

        Email email = Email.to(questionEvent.getSellersEmail())
                           .from(questionEvent.getPossibleBuyer())
                           .subject(subject)
                           .body(body)
                           .product(questionEvent.getProduct())
                           .build();

        sendEmail.send(email);
        emailRepository.save(email);
    }
}
