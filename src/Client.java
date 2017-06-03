
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import helperClasses.ClientCloseApplication;
import helperClasses.ClientPressedGreenButton;
import helperClasses.ClientRequestAnonName;
import helperClasses.ClientSendMessage;
import helperClasses.ClientSendUnlockGreenButton;
import helperClasses.NewPersonOnChatList;

public class Client {
	private boolean walkiePressed = false;
	public static int anoncount = 0;
	public String usernameClient;
	private Socket socket;

	private JFrame frame;
	private JPanel panel;
	private JTextField typeField;
	private JTextArea chatArea;
	private DefaultListModel dfl;
	private JList jlist;
	private JScrollPane jScrollPane;
	private JTextField username;
	private JPasswordField password;
	private JButton login;
	private JButton loginAnon;
	private JLabel usernameLabel, passwordLabel;
	private JButton walkieButton;

	Thread blueButtonThread;

	ObjectOutputStream oos;
	ObjectInputStream ois;

	Thread acceptingMessages;

	Client() throws UnknownHostException, IOException {
		interfataGrafica();
		connectToServer();
		typeMessage("Admin", "Text goes Here");
		guiListeners();
	}

	public void greenButtonPrivilege() {
		typeField.setText("   Ask permission to speak");
		typeField.setEnabled(false);
		walkieButton.setBackground(Color.GREEN);
		walkieButton.setText("Press here to speak");
		walkieButton.setEnabled(true);
		walkiePressed = false;
	}

	public void losePrivilegeToSpeak() {
		typeField.setText("   Ask permission to speak");
		typeField.setEnabled(false);
		walkieButton.setBackground(Color.RED);
		walkieButton.setText("You can't speak right now");
		walkieButton.setEnabled(false);
		walkiePressed = true;

	}

	public void guiListeners() {
		loginAnon.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				walkieButton.setEnabled(true);
				try {
					oos.writeObject(new ClientRequestAnonName());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		});

		walkieButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				long tStart = System.currentTimeMillis();
				long tEnd = 10000;
				if (walkiePressed == false) {
					Runnable timeSpentInBlue = new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							while (System.currentTimeMillis() - tStart < tEnd) {

							}
							walkieButton.setBackground(Color.GREEN);
							typeField.setEnabled(false);
							typeField.setText(" You can't speak right now");
							walkieButton.setText("Press here to talk");
							try {
								oos.writeObject(new ClientSendUnlockGreenButton());
								oos.flush();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
					};
					blueButtonThread = new Thread(timeSpentInBlue);
					blueButtonThread.start();
					walkieButton.setBackground(Color.BLUE);
					walkieButton.setText("Press here to end your speak session");
					typeField.setEnabled(true);
					typeField.setText(" Enter your message here and press enter");
					try {
						oos.writeObject(new ClientPressedGreenButton());
						oos.flush();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					walkiePressed = true;
				} else if (walkiePressed == true) {
					blueButtonThread.stop();
					walkieButton.setBackground(Color.GREEN);
					typeField.setEnabled(false);
					typeField.setText(" You can't speak right now");
					walkieButton.setText("Press here to talk");
					try {
						oos.writeObject(new ClientSendUnlockGreenButton());
						oos.flush();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		});

		typeField.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				// TODO Auto-generated method stub
				typeField.setText("");

			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

		});
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {

				try {
					oos.writeObject(new ClientCloseApplication());
					oos.flush();
					oos.close();
					ois.close();
					socket.close();
					acceptingMessages.stop();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		typeField.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					String message = typeField.getText();
					ClientSendMessage c = new ClientSendMessage();
					c.username = usernameClient;
					c.message = message;
					try {
						oos.writeObject(c);
						oos.flush();
					} catch (IOException asd) {
						// TODO Auto-generated catch block
						asd.printStackTrace();
					}
				}

			}

			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub

			}

		});
	}

	public void typeMessage(String nume, String message) {
		String comp = new String();
		comp += "< " + nume + " > : " + message;
		chatArea.setText(chatArea.getText() + comp + "\n");
	}

	public void connectToServer() throws UnknownHostException, IOException {
		socket = new Socket("localhost", 12345);
		oos = new ObjectOutputStream(socket.getOutputStream());
		ois = new ObjectInputStream(socket.getInputStream());
		oos.flush();

		Runnable acceptingMessage = new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while (true) {
					try {

						Object s = ois.readObject();
						if (s instanceof ClientSendMessage) {
							ClientSendMessage aux = (ClientSendMessage) s;
							typeMessage(aux.username, aux.message);
						}
						if (s instanceof ClientPressedGreenButton) {
							losePrivilegeToSpeak();
						}
						if (s instanceof ClientSendUnlockGreenButton) {
							greenButtonPrivilege();
						}
						if (s instanceof ClientRequestAnonName) {
							ClientRequestAnonName a = (ClientRequestAnonName) s;
							String str = a.nume;
							usernameClient = str;
							login.setEnabled(false);
							loginAnon.setEnabled(false);
							JLabel uname = new JLabel(str);
							panel.remove(username);
							GridBagConstraints c = new GridBagConstraints();
							c.gridx = 1;
							c.gridy = 0;
							c.weightx = 1;
							c.fill = c.HORIZONTAL;
							c.insets = new Insets(5, 5, 5, 5);
							panel.add(uname, c);
							panel.updateUI();
							password.setEnabled(false);
							NewPersonOnChatList npocl = new NewPersonOnChatList();
							npocl.nume = usernameClient;
							oos.writeObject(npocl);
							oos.flush();
						}
						if (s instanceof NewPersonOnChatList) {
							NewPersonOnChatList aux = (NewPersonOnChatList) s;
							dfl.addElement(aux.nume);
						}

					} catch (ClassNotFoundException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}

		};
		acceptingMessages = new Thread(acceptingMessage);
		acceptingMessages.start();
	}

	public void interfataGrafica() {
		frame = new JFrame("Walkie Talkie - Client - Normal User");
		panel = new JPanel();

		frame.setSize(750, 750);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);

		frame.setLayout(new BorderLayout());
		panel.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();

		usernameLabel = new JLabel("Username", JLabel.CENTER);
		passwordLabel = new JLabel("Password", JLabel.CENTER);
		c.gridx = 0;
		c.gridy = 0;

		c.insets = new Insets(5, 5, 5, 5);
		panel.add(usernameLabel, c);
		c.gridy = 1;
		panel.add(passwordLabel, c);

		c = new GridBagConstraints();
		username = new JTextField("");
		password = new JPasswordField(4);
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 1;
		c.fill = c.HORIZONTAL;
		c.insets = new Insets(5, 5, 5, 5);
		panel.add(username, c);
		c.gridy = 1;
		panel.add(password, c);

		login = new JButton("Login");
		loginAnon = new JButton("Login Anon");
		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = 0;
		c.fill = c.BOTH;
		c.insets = new Insets(5, 5, 5, 5);
		panel.add(login, c);
		c.gridy = 1;
		panel.add(loginAnon, c);

		dfl = new DefaultListModel();
		jlist = new JList(dfl);
		jlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jlist.setSelectedIndex(-1);
		jScrollPane = new JScrollPane(jlist);

		c = new GridBagConstraints();
		c.gridx = 4;
		c.gridy = 0;
		c.gridheight = 6;
		c.gridwidth = 1;
		c.weightx = 0.5;
		c.weighty = 1;
		c.fill = c.BOTH;
		panel.add(jScrollPane, c);

		c = new GridBagConstraints();
		chatArea = new JTextArea();
		chatArea.setEditable(false);
		c.gridx = 0;
		c.gridy = 2;
		c.gridheight = 5;
		c.gridwidth = 4;
		c.weightx = 1;
		c.weighty = 1;
		c.insets = new Insets(10, 10, 10, 10);
		c.fill = c.BOTH;

		panel.add(chatArea, c);

		c = new GridBagConstraints();
		typeField = new JTextField("   Press the green button to talk");
		c.gridx = 0;
		c.gridy = 7;
		c.weightx = 2;
		c.gridwidth = 5;
		c.insets = new Insets(10, 10, 10, 10);
		c.fill = c.HORIZONTAL;
		typeField.setEnabled(false);
		panel.add(typeField, c);

		walkieButton = new JButton("Press here to speak");
		walkieButton.setEnabled(false);
		c = new GridBagConstraints();
		walkieButton.setBackground(Color.GREEN);
		c.gridx = 4;
		c.gridy = 6;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = c.BOTH;
		panel.add(walkieButton, c);

		frame.add(panel);
		frame.setVisible(true);
	}

	public static void main(String[] args) throws UnknownHostException, IOException {
		Client c = new Client();
	}

}
