import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;

import helperClasses.ClientCloseApplication;
import helperClasses.ClientPressedGreenButton;
import helperClasses.ClientRequestAnonName;
import helperClasses.ClientSendMessage;
import helperClasses.ClientSendUnlockGreenButton;
import helperClasses.NewPersonOnChatList;

public class Server {
	ServerSocket serverSocket;
	ArrayList<Socket> listSocket;
	ArrayList<ObjectInputStream> listOis;
	ArrayList<ObjectOutputStream> listOos;

	Properties configuration;
	Properties accounts;

	public int nrAnon = 0;

	Server() throws IOException, InterruptedException {
		listSocket = new ArrayList<>();
		listOis = new ArrayList<>();
		listOos = new ArrayList<>();
		startServer();
	}

	public void addToTheUsersListName(NewPersonOnChatList a) throws IOException {

		for (int i = 0; i < listOos.size(); i++) {
			listOos.get(i).writeObject(a);
			listOos.get(i).flush();
		}
	}

	public void clientUnlockGreenButton() throws IOException {
		for (int i = 0; i < listOos.size(); i++) {
			listOos.get(i).writeObject(new ClientSendUnlockGreenButton());
			listOos.get(i).flush();
		}
	}

	public void clientSendMessageFunct(ClientSendMessage a) throws IOException {
		for (int i = 0; i < listOos.size(); i++) {
			listOos.get(i).writeObject(a);
			listOos.get(i).flush();
		}

	}

	public void clientsLosePrivilegeToSpeak(ObjectOutputStream oos) throws IOException {
		for (int i = 0; i < listOos.size(); i++)
			if (listOos.get(i) != oos) {
				listOos.get(i).writeObject(new ClientPressedGreenButton());
				listOos.get(i).flush();
			}

	}

	public void startServer() throws IOException {
		serverSocket = new ServerSocket(12345);
		Runnable acceptingThread = new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while (true) {
					try {
						Socket s = serverSocket.accept();
						ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
						ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
						oos.flush();

						listSocket.add(s);
						listOis.add(ois);
						listOos.add(oos);
						System.out.println("Client connectat");

						Runnable waitingForClientEvent = new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								while (true) {
									try {
										Object primit = ois.readObject();
										if (primit instanceof ClientSendMessage) {
											ClientSendMessage aux = (ClientSendMessage) primit;
											clientSendMessageFunct(aux);

										}
										if (primit instanceof ClientCloseApplication) {
											int i = listSocket.indexOf(s);
											listSocket.get(i).close();
											listOos.get(i).close();
											listOis.get(i).close();
											listSocket.remove(s);
											listOos.remove(oos);
											listOis.remove(ois);
											return;
										}
										if (primit instanceof ClientPressedGreenButton) {
											clientsLosePrivilegeToSpeak(oos);
										}
										if (primit instanceof ClientSendUnlockGreenButton) {
											clientUnlockGreenButton();
										}
										if (primit instanceof ClientRequestAnonName) {
											ClientRequestAnonName a = new ClientRequestAnonName();
											a.nume = "Anonymous(" + nrAnon + ")";
											nrAnon++;
											oos.writeObject(a);
											oos.flush();
										}
										if (primit instanceof NewPersonOnChatList) {
											NewPersonOnChatList o = (NewPersonOnChatList) primit;
											addToTheUsersListName(o);
										}
									} catch (ClassNotFoundException | IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
										return;
									}
								}
							}

						};
						Thread waitingClientThread = new Thread(waitingForClientEvent);
						waitingClientThread.start();

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

			}

		};
		Thread t = new Thread(acceptingThread);
		t.start();
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		Server s = new Server();
	}

}
