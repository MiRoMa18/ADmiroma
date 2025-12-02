package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.config.HibernateUtil;
import org.hibernate.SessionFactory;

public class Main extends Application {
    private static SessionFactory sessionFactory;

    @Override
    public void init() {
        sessionFactory = HibernateUtil.getSessionFactory();
        System.out.println("Hibernate inicializado correctamente");
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
        Scene scene = new Scene(loader.load(), 450, 550);

        stage.setTitle("Control Horario - Login");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    @Override
    public void stop() {
        if (sessionFactory != null) {
            sessionFactory.close();
            System.out.println("Hibernate cerrado correctamente");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}