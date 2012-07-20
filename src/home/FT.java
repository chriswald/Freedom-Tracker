package home;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import pkg.BusyTime;
import pkg.Day;
import pkg.Schedule;

public class FT extends JFrame implements ListSelectionListener, ChangeListener, ActionListener, WindowListener{

	private static final long serialVersionUID = 1L;

	private DefaultListModel model = new DefaultListModel();
	private JList list;
	private JPanel contentPane;
	private JPanel weekPanel;
	private JPanel dayPanel;
	private JPanel mPanel = new JPanel();
	private JPanel tPanel = new JPanel();
	private JPanel wPanel = new JPanel();
	private JPanel rPanel = new JPanel();
	private JPanel fPanel = new JPanel();
	private JTabbedPane tabbedPane;
	private JTabbedPane tabbedPane_days;

	private static final String PROGRAM_UID = "freedomtracker";
	private static final String REQ_TAG = "-req";
	private static final String BASE_URL = "http://dl.dropbox.com/u/4111992/";
	private static final String STATS_FILE = PROGRAM_UID + ".stat";
	private static final String STATS_URL = BASE_URL + STATS_FILE;
	private static final String FILE_LISTING = "index.dir";
	private static final String EXPORT_TAG = "#export";
	private static final String ABOUT_TAG = "#about";
	private static final String NEWS_TAG = "#news";
	private static final String VERSION_TAG = "#VERSION#";
	private static final String VERSION = "2.2.3";
	private static String REQUIRED_FILES_URL = "";
	private static String export_file = "export.ccl";
	private static String about_page_file = "about.html";
	private static String news_page_file = "news.html";
	private String about_page_string = "";
	private String news_page_string = "";

	private Vector<Schedule> schedules = new Vector<Schedule>();
	private int[] selected = new int[0];

	private Vector<String> files = new Vector<String>();
	private Day today = null;

	private int selected_tab = 0;

	private Timer timer = new Timer(500, this);
	private double progression = 0d;

	// Launch the application.
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					@SuppressWarnings("unused")
					FT frame = new FT();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	// Create the frame. Initialize some values such as the current day
	public FT() {
		long start_time = System.currentTimeMillis();
		this.setTitle("Freedom Tracker");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setBounds(100, 100, 800, 400);
		this.contentPane = new JPanel();
		this.contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		this.setContentPane(this.contentPane);
		this.setVisible(true);


		//	USER CONTENT		//
		Image img = null;
		try {
			img = ImageIO.read(new File("icon.bmp"));
			this.setIconImage(img);
		} catch (Exception e) {}

		this.getFileNames();
		this.saveUrl(export_file, REQUIRED_FILES_URL + export_file);
		this.saveUrl(about_page_file, REQUIRED_FILES_URL + about_page_file);
		this.saveUrl(news_page_file, REQUIRED_FILES_URL + news_page_file);
		this.getFile();
		this.getNames();
		this.fillList();
		this.getAboutPage();
		this.getNewsPage();

		this.timer.start();
		this.actionPerformed(null);

		this.about_page_string = this.about_page_string.replace(VERSION_TAG, VERSION);

		//this.writeUsageStats();
		//	END USER CONTENT	//


		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
			System.exit(-1);
		}

		this.list.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		this.list.addListSelectionListener(this);

		this.tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		this.tabbedPane.addChangeListener(this);


		GroupLayout gl_contentPane = new GroupLayout(this.contentPane);
		gl_contentPane.setHorizontalGroup(
				gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
						.addComponent(this.tabbedPane, GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(this.list, GroupLayout.PREFERRED_SIZE, 219, GroupLayout.PREFERRED_SIZE))
				);
		gl_contentPane.setVerticalGroup(
				gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addComponent(this.tabbedPane, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 352, Short.MAX_VALUE)
						.addComponent(this.list, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 352, Short.MAX_VALUE))
				);



		this.tabbedPane_days = new JTabbedPane(JTabbedPane.LEFT);
		this.tabbedPane_days.setBackground(Color.LIGHT_GRAY);
		this.tabbedPane_days.addChangeListener(this);

		this.mPanel = new JPanel();
		this.mPanel.setBackground(Color.WHITE);
		this.tabbedPane_days.addTab("Monday", null, this.mPanel, null);

		this.tPanel = new JPanel();
		this.tPanel.setBackground(Color.WHITE);
		this.tabbedPane_days.addTab("Tuesday", null, this.tPanel, null);

		this.wPanel = new JPanel();
		this.wPanel.setBackground(Color.WHITE);
		this.tabbedPane_days.addTab("Wednesday", null, this.wPanel, null);

		this.rPanel = new JPanel();
		this.rPanel.setBackground(Color.WHITE);
		this.tabbedPane_days.addTab("Thursday", null, this.rPanel, null);

		this.fPanel = new JPanel();
		this.fPanel.setBackground(Color.WHITE);
		this.tabbedPane_days.addTab("Friday", null, this.fPanel, null);


		//	USER CONTENT		//
		Calendar cal = Calendar.getInstance();
		this.today = this.findDay(cal.get(Calendar.DAY_OF_WEEK));

		this.dayPanel = this.findPanelOnDay(cal.get(Calendar.DAY_OF_WEEK));
		this.tabbedPane_days.setSelectedIndex(this.findDayInt());
		//	END USER CONTENT	//


		this.weekPanel = new JPanel();
		this.weekPanel.setBackground(Color.WHITE);
		this.tabbedPane.addTab("Week", null, this.weekPanel, null);
		this.tabbedPane.addTab("Day", null, this.tabbedPane_days, null);

		JTextPane newsPane = new JTextPane();
		newsPane.setEditable(false);
		newsPane.setContentType("text/html");
		newsPane.setText(this.news_page_string);
		this.tabbedPane.addTab("News", null, newsPane, null);

		JTextPane aboutPane = new JTextPane();
		aboutPane.setEditable(false);
		aboutPane.setContentType("text/html");
		aboutPane.setText(this.about_page_string);
		this.tabbedPane.addTab("About", null, aboutPane, null);

		this.contentPane.setLayout(gl_contentPane);

		this.list.requestFocus();
		System.out.println("Setup Complete: Pass " + (System.currentTimeMillis() - start_time) + "ms");
	}

	// Recalculate the time every half second
	public void actionPerformed(ActionEvent evt) {
		Calendar cal = Calendar.getInstance();
		int hour = cal.get(Calendar.HOUR_OF_DAY) - 8;
		int minute = cal.get(Calendar.MINUTE);

		// If the time is earlier than 8AM set time to zero
		if (hour < 0) {
			hour = 0;
			minute = 0;
		}

		this.progression = hour + (minute / 60d);
	}

	// When selected tab changes, repaint
	public void stateChanged(ChangeEvent evt) {
		this.selected_tab = this.tabbedPane.getSelectedIndex();
		this.dayPanel = this.findPanelOnDay(this.tabbedPane_days.getSelectedIndex() + 1);
		this.today = this.findDay(this.tabbedPane_days.getSelectedIndex() + 2);
		this.repaint();
	}

	// Gets files required for program
	// Constructs daisy-chain:
	//	Reads in the file "index.dir" in the base directory
	//	Searches "index.dir" for PROGRAM_UID and any associated tags
	//	Once found, reads in folder location of next "index.dir" file
	//	Repeat until program files are found
	private void getFileNames() {
		System.out.print("Get DLC File Names: ");
		long start_time = System.currentTimeMillis();

		Vector<String> head_dir_file_lines = new Vector<String>();
		Vector<String> req_dir_file_lines = new Vector<String>();
		String req_dir = "";

		head_dir_file_lines = this.getUrlContents(BASE_URL + FILE_LISTING);
		if (head_dir_file_lines == null) {
			System.out.println("Fail " + (System.currentTimeMillis() - start_time) + "ms");
			return;
		}

		//Get directory with the required Freedom Tracker files
		for (String s : head_dir_file_lines) {
			if (s != null && !s.startsWith(";")) {
				if (s.startsWith(PROGRAM_UID + REQ_TAG)) {
					req_dir = s.split("[ ]")[1];
					break;
				}
			}
		}

		REQUIRED_FILES_URL = BASE_URL + req_dir;

		req_dir_file_lines = this.getUrlContents(REQUIRED_FILES_URL + FILE_LISTING);

		if(req_dir_file_lines == null) {
			System.out.println("Fail " + (System.currentTimeMillis() - start_time) + "ms");
			return;
		}

		//Get names of the required Freedom Tracker files
		for (String s : req_dir_file_lines) {
			if (s != null && !s.startsWith(";")) {
				if (s.startsWith(EXPORT_TAG)) {
					export_file = s.split("[ ]")[1];
				}
				if (s.startsWith(ABOUT_TAG)) {
					about_page_file = s.split("[ ]")[1];
				}
				if (s.startsWith(NEWS_TAG)) {
					news_page_file = s.split("[ ]")[1];
				}
			}
		}

		System.out.println("Pass " + (System.currentTimeMillis() - start_time) + "ms");
	}

	// Read in the contents of the .ccl file and save the schedules in memory
	@SuppressWarnings("unchecked")
	private void getFile() {
		System.out.print("Reading Compiled Calendar: ");
		long start_time = System.currentTimeMillis();

		ObjectInputStream fi;
		try {
			fi = new ObjectInputStream(new BufferedInputStream(new FileInputStream(export_file)));
			this.schedules = (Vector<Schedule>) fi.readObject();
			fi.close();
		} catch (Exception e) {
			System.out.println("Fail " + (System.currentTimeMillis() - start_time) + "ms");
			return;
		}

		System.out.println("Pass " + (System.currentTimeMillis() - start_time) + "ms");
	}

	// Read in the .html file containing the version and disclaimer info
	private void getAboutPage() {
		System.out.print("Reading HTML about page: ");
		long start_time = System.currentTimeMillis();

		BufferedReader fi;
		try {
			fi = new BufferedReader(new FileReader(about_page_file));
			this.about_page_string = "";
			String line = "";
			while ((line = fi.readLine()) != null) {
				this.about_page_string += line;
			}
		} catch (Exception e) {
			this.about_page_string = "The system could not find the About page.<br>Please connect to the internet and re-run this program to download the latest version.";
			System.out.println("Fail " + (System.currentTimeMillis() - start_time) + "ms");
			return;
		}

		System.out.println("Pass " + (System.currentTimeMillis() - start_time) + "ms");
	}

	// Read in the .html file containing some news about upcoming group events
	private void getNewsPage() {
		System.out.print("Reading HTML news page: ");
		long start_time = System.currentTimeMillis();

		BufferedReader fi;
		try {
			fi = new BufferedReader(new FileReader(news_page_file));
			this.news_page_string = "";
			String line = "";
			while ((line = fi.readLine()) != null) {
				this.news_page_string += line;
			}
		} catch (Exception e) {
			this.news_page_string = "The system could not find the News page.<br>Please connect to the internet and re-run this program to download the latest version.";
			System.out.println("Fail " + (System.currentTimeMillis() - start_time) + "ms");
			return;
		}

		System.out.println("Pass " + (System.currentTimeMillis() - start_time) + "ms");
	}

	// Read in the names from all schedules
	private void getNames() {
		long start_time = System.currentTimeMillis();
		System.out.print("Reading names from schedules: ");
		System.out.print((this.schedules.size() == 0) ? "Fail " : "Pass ");

		for (Schedule s : this.schedules) {
			this.files.add(s.name);
		}

		System.out.println((System.currentTimeMillis() - start_time) + "ms");
	}

	// Read in names from all of the individual schedules from the .ccl file
	// Build the displayed list based on found names
	private void fillList() {
		long start_time = System.currentTimeMillis();
		System.out.print("Populating list: ");
		System.out.print((this.files.size() == 0) ? "Fail " : "Pass ");

		for (String s : this.files) {
			this.model.addElement(s);
		}

		this.list = new JList(this.model);

		System.out.println((System.currentTimeMillis() - start_time) + "ms");
	}

	// When selected list values change repaint the screen
	@Override
	public void valueChanged(ListSelectionEvent evt) {
		if (!evt.getValueIsAdjusting()) {
			this.selected = this.list.getSelectedIndices();
			this.repaint();
		}
	}

	// When the window is restored from an iconified state to a normal one repaint the screen
	@Override
	public void windowDeiconified(WindowEvent evt) {
		this.repaint();
	}

	// Generate a pseudo-random color based on the index of a user's name
	private void makeColor(int[] rgb, int x) {
		rgb[0] = (int) (50 * Math.sin(20 * x) + 125);
		rgb[1] = (int) (75 * Math.cos(60 * x) + 75);
		rgb[2] = (int) (150 * Math.sin(10 * x) + 200);

		rgb[0] = Math.abs(rgb[0] % 255);
		rgb[1] = Math.abs(rgb[1] % 255);
		rgb[2] = Math.abs(rgb[2] % 255);
	}

	// Gets a pseudo-random color from makeColor() and applies an alpha of 90 to it.
	private Color makeFillColor(int x) {
		int[] rgb = new int[3];
		this.makeColor(rgb, x);
		return new Color(rgb[0], rgb[1], rgb[2], 90);
	}

	// Gets a pseudo-random color from makeColor() and applies an alpha of 225 to it.
	private Color makeBorderColor(int x) {
		int[] rgb = new int[3];
		this.makeColor(rgb, x);
		return new Color(rgb[0], rgb[1], rgb[2], 225);
	}

	// Primary switch for redirecting control to proper components
	@Override
	public void paint(Graphics g) {
		super.paint(g);

		switch (this.selected_tab) {
			case 0:
				this.paintWeek();
				break;
			case 1:
				this.paintDay();
				break;
			default:
				break;
		}
	}

	// Draws a weekly schedule view
	private void paintWeek() {
		Graphics2D g2d = (Graphics2D) this.weekPanel.getGraphics();

		//Set up double buffering
		Image buffer = this.createImage(this.weekPanel.getWidth(), this.weekPanel.getHeight());
		Graphics2D tmp = (Graphics2D) buffer.getGraphics();
		tmp.setColor(Color.white);
		tmp.fillRect(0, 0, buffer.getWidth(null), buffer.getHeight(null));

		int w = this.weekPanel.getWidth();
		int h = this.weekPanel.getHeight();

		tmp.setColor(Color.black);

		//Draw Vertical Lines
		for (int i = 0; i < 6; i ++) {
			int x = this.getPosxOfTime_week(i);
			int y0 = 0;
			int y1 = h;
			tmp.drawLine(x, y0, x, y1);
		}

		//Draw Horizontal Lines
		for (int i = 0; i < 15; i ++) {
			int y = this.getPosyOfTime_week(i + 8, 0);
			tmp.drawLine(0, y, w, y);
		}

		// Draw the weekly schedules for all selected users
		for (int i : this.selected) {
			Schedule s = this.schedules.get(i);
			Color fill_color = this.makeFillColor(i);
			Color border_color = this.makeBorderColor(i);
			for (BusyTime bt : s.classes) {

				// Convert the day of a class to a constant multiplier
				int day_index = 0;
				switch (bt.day){
					case Monday:
						day_index = 0;
						break;
					case Tuesday:
						day_index = 1;
						break;
					case Wednesday:
						day_index = 2;
						break;
					case Thursday:
						day_index = 3;
						break;
					case Friday:
						day_index = 4;
						break;
				}

				//Calculate coords and dimensions for the current class's box
				int start = this.getPosyOfTime_week(bt.time.start_hour, bt.time.start_minute);
				int height = this.getPosyOfTime_week(bt.time.end_hour, bt.time.end_minute) - start;

				int x = this.getPosxOfTime_week(day_index);
				int width = this.getPosxOfTime_week(day_index + 1) - x;

				// Draw a filled rectangle with a border for the current class on the current user
				tmp.setColor(fill_color);
				tmp.fillRect(x, start, width, height);
				tmp.setColor(border_color);
				tmp.drawRect(x, start, width, height);
			}
		}

		// Draw hours
		tmp.setColor(Color.black);
		for (int i = 0; i < 15; i ++) {
			String hour = "";
			hour += (((i + 6) % 12) + 1) + ":00";

			tmp.drawString(hour, 2, (int) ((i - 1) * (h / 14d) + 13d));
		}

		// Draw a red line indicating the current time
		if (this.progression > 0) {
			tmp.setColor(Color.red);
			double hour_height = (h / 14d);
			int current_time_y = (int) (this.progression * hour_height);
			tmp.drawLine(0, current_time_y, w, current_time_y);
		}

		g2d.drawImage(buffer, 0, 0, null);
	}

	// Draws a daily schedule view
	private void paintDay() {
		Graphics2D g2d = (Graphics2D) this.dayPanel.getGraphics();
		Image buffer = this.createImage(this.dayPanel.getWidth(), this.dayPanel.getHeight());
		Graphics2D tmp = (Graphics2D) buffer.getGraphics();
		tmp.setColor(Color.white);
		tmp.fillRect(0, 0, buffer.getWidth(null), buffer.getHeight(null));

		double num_hours = 14d;
		int pane_w = this.dayPanel.getWidth();
		int pane_h = this.dayPanel.getHeight();
		double xoffs = 150;
		double item_height = 40;
		double item_width = (pane_w - xoffs) / num_hours;

		// Scale rows to fit inside the pane
		if (item_height * this.selected.length > pane_h) {
			item_height = pane_h / this.selected.length;
		}

		// Constrain rows to be no more than 20 px small
		if (item_height < 20) {
			item_height = 20;
		}

		tmp.setColor(Color.black);

		//Draw vertical Lines
		if (this.selected.length > 0) {
			for (int i = 0; i < num_hours; i ++) {
				double x = this.getPosOfTime_day(i + 8, 0);
				double y = this.selected.length * item_height;

				tmp.drawLine((int) x, 0, (int) x, (int) y);
			}
		}

		if (this.selected.length > 0)
			tmp.drawLine(0, 0, pane_w, 0);

		//Draw horizontal elements
		for (int i = 1; i <= this.selected.length; i ++) {
			tmp.setColor(Color.black);
			//Draw name and horizontal line
			double y = (i * item_height);
			String name = this.schedules.get(this.selected[i - 1]).name;
			tmp.drawString(name, 2, (int) (i * item_height - 6d));
			tmp.drawLine(0, (int) y, pane_w, (int) y);

			Vector<BusyTime> bt = new Vector<BusyTime>();
			for (BusyTime b : this.schedules.get(this.selected[i - 1]).classes) {
				if (b.day == this.today)
					bt.add(b);
			}

			tmp.setColor(new Color (125, 125, 125, 90));

			for (BusyTime b : bt) {
				double start = this.getPosOfTime_day(b.time.start_hour, b.time.start_minute);
				double width = this.getPosOfTime_day(b.time.end_hour, b.time.end_minute) - start;

				double y0 = ((i - 1) * item_height);
				double y1 = (i * item_height);

				double height = y1 - y0;

				tmp.setColor(this.makeFillColor(this.selected[i - 1]));
				tmp.fillRect((int) start, (int) y0, (int) width, (int) height);
				tmp.setColor(this.makeBorderColor(this.selected[i - 1]));
				tmp.drawRect((int) start, (int) y0, (int) width, (int) height);
			}
		}

		if (this.progression > 0) {
			tmp.setColor(Color.red);
			int current_time_x = (int) (xoffs + (item_width * this.progression));
			tmp.drawLine(current_time_x, 0, current_time_x, (int) (item_height * this.selected.length));
		}

		// Draw the hours
		tmp.setColor(Color.black);
		if (this.selected.length > 0) {
			for (int i = 0; i < num_hours; i ++) {
				double x = this.getPosOfTime_day(i + 8, 0);

				String hour = "";
				hour += ((i + 7) % 12) + 1;

				tmp.drawString(hour, (int) (x + 2), 12);
			}
		}

		//super.paint(this.getGraphics());
		g2d.drawImage(buffer, 0, 0, null);
	}

	// Finds the X position of where a day is to be drawn
	//	Used for scaling
	private int getPosxOfTime_week(int day) {
		double width = this.weekPanel.getWidth();
		final double NUM_DAYS = 5d;
		double day_step = width / NUM_DAYS;

		return (int) (day * day_step);
	}

	// Finds the Y position of where a day is to be drawn
	//	Used for scaling
	private int getPosyOfTime_week(int hour, int minute) {
		double height = this.weekPanel.getHeight();
		final double NUM_HOURS = 14d;
		double hour_step = height / NUM_HOURS;

		double progress = (hour - 8) + (minute / 60d);

		return (int) (progress * hour_step);
	}

	private int getPosOfTime_day(int hour, int minute) {
		double xoffs = 150d;
		double working_width = this.dayPanel.getWidth() - xoffs;
		final double NUM_HOURS = 14d;
		double hour_step = working_width / NUM_HOURS;

		double progress = (hour - 8) + (minute / 60d);

		return (int) (xoffs + (progress * hour_step));
	}

	// Converts an int to a Day
	private Day findDay(int d) {
		switch (d) {
			case 1:
			case 2:
			case 7:
				return Day.Monday;
			case 3:
				return Day.Tuesday;
			case 4:
				return Day.Wednesday;
			case 5:
				return Day.Thursday;
			case 6:
				return Day.Friday;
			default:
				return Day.Monday;
		}
	}

	// Converts a Day to an int
	private int findDayInt() {
		switch (this.today){
			case Monday:
				return 0;
			case Tuesday:
				return 1;
			case Wednesday:
				return 2;
			case Thursday:
				return 3;
			case Friday:
				return 4;
			default:
				return 0;
		}
	}

	// Returns the panel corresponding to the day value
	private JPanel findPanelOnDay(int d) {
		switch (d) {
			case 1:
			case 2:
			case 7:
				return this.mPanel;
			case 3:
				return this.tPanel;
			case 4:
				return this.wPanel;
			case 5:
				return this.rPanel;
			case 6:
				return this.fPanel;
			default:
				return this.mPanel;
		}
	}

	// Read in the contents of a text file at a URL
	private Vector<String> getUrlContents(String urlString) {
		Vector<String> lines = new Vector<String>();
		String line = "";
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(new URL(urlString).openStream()));
			while ((line = in.readLine()) != null) {
				lines.add(line);
			}
		} catch (Exception e) {
			return null;
		}

		return lines;
	}

	// Saves a file from a URL
	private void saveUrl(String filename, String urlString) {
		long start_time = System.currentTimeMillis();
		System.out.print("Retrieving " + filename + ": ");
		BufferedInputStream in = null;
		FileOutputStream fout = null;
		try {
			in = new BufferedInputStream(new URL(urlString).openStream());
			fout = new FileOutputStream(filename);

			byte data[] = new byte[1024];
			int count;
			while ((count = in.read(data, 0, 1024)) != -1) {
				fout.write(data, 0, count);
			}

			in.close();
			fout.close();
		} catch (Exception e) {
			System.out.println("Fail " + (System.currentTimeMillis() - start_time) + "ms");
			return;
		}

		System.out.println("Pass " + (System.currentTimeMillis() - start_time) + "ms");
	}

	// Writes program usage statistics to a URL
	//	Reads in freedomtracker.stat
	//	Compares computer's MAC address with all in file
	//	If not found, adds
	//	Else, skips
	@SuppressWarnings("unused")
	private void writeUsageStats() {
		long start_time = System.currentTimeMillis();
		System.out.print("Updating usage statistics: ");
		Vector<String> current_macs = this.getUrlContents(STATS_URL);

		try {
			InetAddress ip = InetAddress.getLocalHost();
			NetworkInterface net = NetworkInterface.getByInetAddress(ip);
			byte[] mac = net.getHardwareAddress();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < mac.length; i++) {
				sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));
			}

			String mac_adr = sb.toString();

			for (String s : current_macs) {
				if (s.equals(mac_adr))
					return;
			}

			URL url = new URL(STATS_URL);
			URLConnection uc = url.openConnection();
			uc.setDoOutput(true);

			OutputStreamWriter out = new OutputStreamWriter(new BufferedOutputStream(uc.getOutputStream()));
			out.append(mac_adr);

			out.close();
		} catch (Exception e) {
			System.out.println("Fail " + (System.currentTimeMillis() - start_time) + "ms");
			return;
		}

		System.out.println("Pass " + (System.currentTimeMillis() - start_time) + "ms");
	}


	//	UNUSED		//
	public void windowActivated(WindowEvent arg0) {}
	public void windowClosed(WindowEvent arg0) {}
	public void windowClosing(WindowEvent arg0) {}
	public void windowDeactivated(WindowEvent arg0) {}
	public void windowIconified(WindowEvent arg0) {}
	public void windowOpened(WindowEvent arg0) {}
	//	END UNUSED	//
}
