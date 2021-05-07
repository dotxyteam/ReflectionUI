package xy.reflect.ui.example;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;
import xy.reflect.ui.control.swing.editor.StandardEditorBuilder;

public class Library implements Serializable {

	public static void main(String[] args) {
		final Library library = new Library();

		final File libraryFile = new File(
				System.getProperty("custom-reflection-ui-examples.project.directory", "./") + "library.db");
		if (libraryFile.exists()) {
			library.load(libraryFile);
		}
		
		CustomizedUI ui = new CustomizedUI();
		SwingCustomizer renderer = new SwingCustomizer(ui,
				System.getProperty("custom-reflection-ui-examples.project.directory", "./") + "library.icu");
		StandardEditorBuilder windowBuilder = renderer.getEditorBuilder(null, library, null, null, false);
		windowBuilder.createAndShowFrame();
		windowBuilder.getCreatedFrame().addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				library.save(libraryFile);
			}
		});
	}

	private static final long serialVersionUID = 1L;

	private List<Book> books = new ArrayList<Book>();
	private List<Student> students = new ArrayList<Student>();
	private List<Issue> issues = new ArrayList<Issue>();

	public List<Book> getBooks() {
		return books;
	}

	public void setBooks(List<Book> books) {
		this.books = books;
	}

	public List<Student> getStudents() {
		return students;
	}

	public void setStudents(List<Student> students) {
		this.students = students;
	}

	public List<Issue> getIssues() {
		return issues;
	}

	public void setIssues(List<Issue> issues) {
		this.issues = issues;
	}

	public void newIssue(Student s, Book b) {
		Issue issue = new Issue();
		issue.setStudent(s);
		issue.setBook(b);
		issue.setDate(new Date());
		this.issues.add(issue);
	}

	public int getTotalBooks() {
		return books.size();
	}

	public int getTotalStudents() {
		return students.size();
	}

	public int getTotalIssues() {
		return issues.size();
	}

	public int getTotalNonReturnedBooks() {
		int result = 0;
		for (Issue i : issues) {
			if (i.getReturnedDate() != null) {
				result++;
			}
		}
		return result;
	}

	public List<Book> getNMostPopularBooks(int n) {
		List<Book> result = new ArrayList<Book>(books);
		Collections.sort(result, new Comparator<Book>() {
			@Override
			public int compare(Book b1, Book b2) {
				return new Integer(getIssueCount(b1)).compareTo(getIssueCount(b2));
			}

			private Integer getIssueCount(Book b) {
				int result = 0;
				for (Issue i : issues) {
					if (i.getBook() == b) {
						result++;
					}
				}
				return result;
			}
		});
		while (result.size() > n) {
			result.remove(result.size() - 1);
		}
		return result;
	}

	public void save(File file) {
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(file));
			oos.writeObject(this);
		} catch (Throwable t) {
			throw new RuntimeException("Failed to serialize object: " + t.toString());
		} finally {
			try {
				oos.close();
			} catch (Exception ignore) {
			}
		}
	}

	public void load(File file) {
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new FileInputStream(file));
			Library loaded = (Library) ois.readObject();
			books = loaded.books;
			students = loaded.students;
			issues = loaded.issues;
		} catch (Throwable t) {
			throw new RuntimeException("Failed to deserialize object: " + t.toString());
		} finally {
			try {
				ois.close();
			} catch (Exception ignore) {
			}
		}
	}

	public static class Book implements Serializable, Comparable<Book> {

		private static final long serialVersionUID = 1L;

		private String title;
		private String author;

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getAuthor() {
			return author;
		}

		public void setAuthor(String author) {
			this.author = author;
		}

		public String toString() {
			return title;
		}

		@Override
		public int compareTo(Book other) {
			int result = 0;

			if ((title != null) && (other.title == null)) {
				result = 1;
			}
			if ((title == null) && (other.title != null)) {
				result = -1;
			}
			if ((title != null) && (other.title != null)) {
				result = title.compareTo(other.title);
			}
			if (result != 0) {
				return result;
			}

			if ((author != null) && (other.author == null)) {
				result = 1;
			}
			if ((author == null) && (other.author != null)) {
				result = -1;
			}
			if ((author != null) && (other.author != null)) {
				result = author.compareTo(other.author);
			}
			if (result != 0) {
				return result;
			}

			return 0;
		}
	}

	public static class Student implements Serializable, Comparable<Student> {

		private static final long serialVersionUID = 1L;

		private String firstName;
		private String lastName;
		private String phoneNumber;

		public String getFirstName() {
			return firstName;
		}

		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}

		public String getLastName() {
			return lastName;
		}

		public void setLastName(String lastName) {
			this.lastName = lastName;
		}

		public String getPhoneNumber() {
			return phoneNumber;
		}

		public void setPhoneNumber(String phoneNumber) {
			this.phoneNumber = phoneNumber;
		}

		public String toString() {
			return firstName + " " + lastName;
		}

		@Override
		public int compareTo(Student other) {
			int result = 0;

			if ((firstName != null) && (other.firstName == null)) {
				result = 1;
			}
			if ((firstName == null) && (other.firstName != null)) {
				result = -1;
			}
			if ((firstName != null) && (other.firstName != null)) {
				result = firstName.compareTo(other.firstName);
			}
			if (result != 0) {
				return result;
			}

			if ((lastName != null) && (other.lastName == null)) {
				result = 1;
			}
			if ((lastName == null) && (other.lastName != null)) {
				result = -1;
			}
			if ((lastName != null) && (other.lastName != null)) {
				result = lastName.compareTo(other.lastName);
			}
			if (result != 0) {
				return result;
			}

			return 0;
		}
	}

	public static class Issue implements Serializable, Comparable<Issue> {

		private static final long serialVersionUID = 1L;

		private Book book;
		private Student student;
		private Date date;
		private Date returnedDate;

		public Book getBook() {
			return book;
		}

		public void setBook(Book book) {
			this.book = book;
		}

		public Student getStudent() {
			return student;
		}

		public void setStudent(Student student) {
			this.student = student;
		}

		public Date getDate() {
			return date;
		}

		public void setDate(Date date) {
			this.date = date;
		}

		public Date getReturnedDate() {
			return returnedDate;
		}

		public void setReturnedDate(Date returnedDate) {
			this.returnedDate = returnedDate;
		}

		public String toString() {
			return book + " issued to " + student;
		}

		@Override
		public int compareTo(Issue other) {
			int result = 0;

			if ((returnedDate != null) && (other.returnedDate == null)) {
				result = 1;
			}
			if ((returnedDate == null) && (other.returnedDate != null)) {
				result = -1;
			}
			if ((returnedDate != null) && (other.returnedDate != null)) {
				result = returnedDate.compareTo(other.returnedDate);
			}
			if (result != 0) {
				return result;
			}

			if ((date != null) && (other.date == null)) {
				result = 1;
			}
			if ((date == null) && (other.date != null)) {
				result = -1;
			}
			if ((date != null) && (other.date != null)) {
				result = date.compareTo(other.date);
			}
			if (result != 0) {
				return result;
			}

			return 0;
		}
	}

}
