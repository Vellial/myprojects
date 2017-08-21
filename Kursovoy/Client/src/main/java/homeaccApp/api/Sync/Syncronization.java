package homeaccApp.api.Sync;

import org.java_websocket.WebSocket;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayDeque;

/**
 * Singleton class for sync.
 */
public class Syncronization {
    private static Syncronization instance = new Syncronization();
    private homeaccWSCClient tread;

    public Syncronization() {
        connectToWebServer();
    }

    public void connectToWebServer() {
        try {
            tread = new homeaccWSCClient(new URI("ws://localhost:8025/server/end"));
            tread.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static Syncronization getInstance() {
        return instance;
    }

    public void sendMessage(String message) {
        ArrayDeque<String> dequeMessages = new ArrayDeque<String>();
        System.out.println(tread.getReadyState());
        if (tread.getReadyState() == WebSocket.READYSTATE.OPEN) {
            // если есть очередь то сперва отправь мессаджи очереди а потом отправь текущий мессадж
            if (!dequeMessages.isEmpty()) {
                // peek возвращает без удаления элемент из начала очереди. Если очередь пуста, возвращает значение null
                while (dequeMessages.peek() != null) {
                    // извлечение c начала
                    tread.send(dequeMessages.pop());
                }
            }
            tread.send(message);
        }
        else {
            // Заполни очередь message

            // стандартное добавление элементов
            dequeMessages.add(message);
//            Alert alert = new Alert(Alert.AlertType.INFORMATION);
//           // alert.initOwner(billsTable.getScene().getWindow());
//            alert.setTitle("Синхронизация не выполнена");
//            alert.setHeaderText("Сервер временно недоступен. Попробуйте позже.");
//            alert.setContentText("Пожалуйста, повторите попытку позже");
//
//            alert.showAndWait();
        }
    }

    public WebSocket.READYSTATE getReadyState() {
        return tread.getReadyState();
    }

}
