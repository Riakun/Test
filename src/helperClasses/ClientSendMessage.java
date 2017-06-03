package helperClasses;

import java.io.Serializable;

public class ClientSendMessage implements Serializable {
	public String username;
	public String message;

	public ClientSendMessage(){
		username="Anonymoum";
		message="Blank";
	}

}
