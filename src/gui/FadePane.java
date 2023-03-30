package gui;

import application.Distillat;
import application.Fad;
import application.Lager;
import controller.Controller;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

//TODO Opdater FadListView når Fad oprettes (ikke clear alt)
//TODO Tilføj "CheckBoxes": "Vis kun tomme fade", "Vis kun fyldte fade"

public class FadePane extends GridPane {
    private Controller controller = Controller.getController();

    private ListView<Fad> listFade = new ListView<>();

    private TextField txtxFadtype = new TextField();
    private Lager valgtLager;
    private TextField txtfadStr= new TextField();
    private Label lblfadType= new Label("Fad Type");
    private Label lblfadStr = new Label("Fad størrelse");
    private Label lblvealglager = new Label("Vælg lager:");
    private Label lblError = new Label();
    private Label lbltotalfad=new Label("Total fade: "+controller.totalAntalFad());
    private ComboBox comboBoxLager = new ComboBox<>();
    private Button btnopretfad = new Button("Opret fad");

    public FadePane() {

        this.setPadding(new Insets(20));
        this.setHgap(20);
        this.setVgap(10);
        this.setGridLinesVisible(false);

        //----------------------------------------------list
        this.comboBoxLager.getItems().addAll(controller.getAlleLagre());
        this.add( comboBoxLager, 3, 0);

        comboBoxLager.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            listFade.getSelectionModel().clearSelection(); // Clear the ListView selection
            Lager lager = (Lager) newSelection;
            if (lager != null) {
                listFade.getItems().setAll(controller.getFadeILager(lager.getId()));
            } else {
                listFade.getItems().clear(); // Clear the ListView if no Lager is selected
            }
        });


        //-----------------------------------------------Fade liste
        this.add(listFade, 1, 3);
        this.listFade.setEditable(false);
        this.valgtLager = (Lager)comboBoxLager.getValue();
        if (valgtLager != null) {
            System.out.println(valgtLager.getId());
            this.listFade.getItems().setAll(controller.getFadeILager(valgtLager.getId()));
        }
        this.listFade.setMinHeight(200);



        //------------------------------------------------- Labels
        this.add(lblfadStr,0,0);
        this.add(lblfadType,0,1);
        this.add(lblvealglager,2,0);
        this.add(lblError,3,2);
        lblError.setStyle("-fx-text-fill: red;");
        this.add(lbltotalfad, 0, 7);

        //------------------------------------------------button
        this.add(btnopretfad,3,1);
        btnopretfad.setOnAction(event -> btnOpretAction());

        //-------------------------------------------------text
        this.add(txtfadStr,1,0);
        this.add(txtxFadtype, 1, 1);


        //-------------------------------------------------






    }
    //TODO lav input validering
    private void btnOpretAction() {
        lblError.setText("");
        int plads = 1;
        Lager lager = (Lager)comboBoxLager.getValue();
        String fadtype = txtxFadtype.getText().trim();
        int fadStr = Integer.parseInt(txtfadStr.getText().trim());
        if (fadtype.length() == 0 || fadStr == 0 || txtfadStr.getText().trim().length() == 0) {
            lblError.setText("Udfyld venligst alle felter");
        } else {
            controller.opretFad(fadtype, fadStr, lager, plads);
            txtfadStr.clear();
            txtxFadtype.clear();
            comboBoxLager.getSelectionModel().clearSelection();
            this.updateControls();
        }
    }

    public void updateControls() {
        lbltotalfad.setText("Total fade: "+controller.totalAntalFad());
        comboBoxLager.getItems().clear();
        comboBoxLager.getItems().addAll(controller.getAlleLagre());
        if (this.valgtLager != null) {
            listFade.getItems().addAll(controller.getFadeILager(valgtLager.getId()));
        }


    }

}
