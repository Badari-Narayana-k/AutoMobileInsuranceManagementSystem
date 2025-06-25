package com.aims.prod.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class MailService {
	
	@Autowired
	private JavaMailSender mailSender;
	@Async
	public void sendClaimStatusEmail(String to,String name,String policyName,String status) {
		String subject="Your claim Status for "+policyName;
		String message="Dear"+name+",\n\n"+"Your claim for the policy \""+policyName+"\" has been "+status.toUpperCase()+"Thank you for using our service.\n\n"+"Regards, \nInsurance Portal Team";
		SimpleMailMessage email=new SimpleMailMessage();
		email.setTo(to);
		email.setSubject(subject);
		email.setText(message);
		mailSender.send(email);
	}

}
