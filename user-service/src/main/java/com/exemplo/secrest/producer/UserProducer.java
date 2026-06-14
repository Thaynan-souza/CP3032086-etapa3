package com.exemplo.secrest.producer;

import com.exemplo.secrest.dto.EmailDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UserProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${broker.queue.email.name}")
    private String routingKey;

    public void publishMessageEmail(EmailDto emailDto) {
        // Envia a mensagem para a fila default.email
        rabbitTemplate.convertAndSend("", routingKey, emailDto);
    }
}