package xy.reflect.ui.example;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;
import xy.reflect.ui.util.MoreSystemProperties;

/**
 * Mail client GUI generated with the XML declarative customizations.
 * 
 * @author olitank
 *
 */
public class MailClient {

	public static void main(String[] args) throws IOException {
		System.out.println("Set the following system property to disable the design mode:\n-D"
				+ MoreSystemProperties.HIDE_INFO_CUSTOMIZATIONS_TOOLS + "=true");

		CustomizedUI reflectionUI = new CustomizedUI();
		SwingCustomizer renderer = new SwingCustomizer(reflectionUI,
				System.getProperty("custom-reflection-ui-examples.project.directory", "./") + "mailClient.icu");
		renderer.openObjectFrame(new MailClient());
	}

	private List<Message> inbox = Arrays.asList(
			new Message("test@address.com", new Date(), "subject1", "body\nbody\nbody"),
			new Message("test@address.com", new Date(), "subject2", "body\nbody\nbody"),
			new Message("test@address.com", new Date(), "subject3", "body\nbody\nbody"),
			new Message("test@address.com", new Date(), "subject4", "body\nbody\nbody"));
	private List<Message> sent;
	private List<Message> draft;
	private List<Message> trash;

	public List<Message> getInbox() {
		return inbox;
	}

	public void setInbox(List<Message> inbox) {
		this.inbox = inbox;
	}

	public List<Message> getSent() {
		return sent;
	}

	public void setSent(List<Message> sent) {
		this.sent = sent;
	}

	public List<Message> getDraft() {
		return draft;
	}

	public void setDraft(List<Message> draft) {
		this.draft = draft;
	}

	public List<Message> getTrash() {
		return trash;
	}

	public void setTrash(List<Message> trash) {
		this.trash = trash;
	}

	public static class Message {

		private String from;
		private Date date;
		private String subject;
		private String body;

		public Message(String from, Date date, String subject, String body) {
			super();
			this.from = from;
			this.date = date;
			this.subject = subject;
			this.body = body;
		}

		public String getFrom() {
			return from;
		}

		public void setFrom(String from) {
			this.from = from;
		}

		public Date getDate() {
			return date;
		}

		public void setDate(Date date) {
			this.date = date;
		}

		public String getSubject() {
			return subject;
		}

		public void setSubject(String subject) {
			this.subject = subject;
		}

		public String getBody() {
			return body;
		}

		public void setBody(String body) {
			this.body = body;
		}

		public void reply() {

		}

		public void forward() {

		}

	}

}
