package homeaccApp.api.Sync;

import java.io.IOException;

/**
 * Thread for websockets
 */
//public class NewThread implements Runnable {
//    Thread thread;
//    private homeaccWSCClient wscClient;
//
//    // Конструктор
//    NewThread() {
//        // Создаём новый второй поток
//        wscClient = new homeaccWSCClient();
//        thread = new Thread(this, "WebSocketsThread");
//        thread.start(); // Запускаем поток
//    }
//
//    // Обязательный метод для интерфейса Runnable
//    public synchronized void run() {
//        synchronized (wscClient) {
//            wscClient.connectToWebServer();
//        }
//    }
//
//    public void sendMessage(String message) throws IOException {
//        wscClient.sendMessage(message);
//    }
//}
