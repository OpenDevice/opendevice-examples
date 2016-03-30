package br.com.criativasoft.intellihouse;

public interface Constants {
	
	public static interface Settings{   
		public static final String ENABLE_WEB_SERVICE = "ENABLE_WEB_SERVICE"; // Ativa WebService Local.
		public static final String WEB_SERVICE_PORT = "WEB_SERVICE_PORT"; // Ativa WebService Local.
		
		public static final String ENABLE_REMOTE_SERVER = "ENABLE_REMOTE_SERVER"; // Ativa WebService Remoto.
		public static final String REMOTE_SERVER = "REMOTE_SERVER"; 
		
		/** Automatically connect to the control module that Bluetooth is active */
		public static final String BLUETOOTH_AUTO_CONNECT = "BLUETOOTH_AUTO_CONNECT";
		
		/** Automatically turn off if no bluetooth control module is active (battery save) */
		public static final String BLUETOOTH_AUTO_DISCONNECT = "BLUETOOTH_AUTO_DISCONNECT"; 
		
		// TODO: remover quando tiver a implementação dos CRUD de conexao.
		public static final String BLUETOOTH_DEFAULT= "BLUETOOTH_DEFAULT"; 
	}

}
