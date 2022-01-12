package karstenroethig.springbootblueprint.webapp.service.impl;

import java.net.URI;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import karstenroethig.springbootblueprint.webapp.model.dto.auth.UserDto;
import karstenroethig.springbootblueprint.webapp.util.MessageKeyEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Service
public class EmailServiceImpl
{
	@Autowired private JavaMailSender emailSender;

	@Autowired private SpringTemplateEngine templateEngine;

	@Autowired private MessageSource messageSource;

	@Value("classpath:/static/images/brand.png")
	private Resource brandResourceFile;

	@Getter
	@RequiredArgsConstructor
	private enum MailTypeEnum
	{
		REGISTRATION_CONFIRM("mail/registration-confirm.html", MessageKeyEnum.MAIL_REGISTRATION_CONFIM_SUBJECT);

		private final String template;
		private final MessageKeyEnum subjectMessageKey;
	}

	public void sendRegistrationConfirmMessage(UserDto user, Locale locale, URI registrationConfirmUri) throws MessagingException
	{
		Map<String, Object> templateVariables = new HashMap<>();
		templateVariables.put("user", user);
		templateVariables.put("registrationConfirmUrl", registrationConfirmUri.toString());

		sendMessageUsingTemplate(user.getEmail(), MailTypeEnum.REGISTRATION_CONFIRM, locale, templateVariables);
	}

	public void sendMessageUsingTemplate(String to, MailTypeEnum mailType, Locale locale, Map<String, Object> templateVariables) throws MessagingException
	{
		String subject = messageSource.getMessage(mailType.getSubjectMessageKey().getKey(), null, locale);

		Context thymeleafContext = new Context(locale);
		thymeleafContext.setVariables(templateVariables);

		String htmlBody = templateEngine.process(mailType.getTemplate(), thymeleafContext);

		sendHtmlMessage(to, subject, htmlBody);
	}

	private void sendHtmlMessage(String to, String subject, String htmlBody) throws MessagingException
	{
		MimeMessage message = emailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
		helper.setTo(to);
		helper.setSubject(subject);
		helper.setText(htmlBody, true);
		helper.addInline("brand.png", brandResourceFile);

		emailSender.send(message);
	}
}
