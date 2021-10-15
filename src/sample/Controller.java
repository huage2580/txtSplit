package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller {

    public Label path;
    public TextArea regex;
    public ListView<String> list;
    private Stage stage;
    private File file;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void choose(ActionEvent event)
    {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("选择TXT文件");
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("txt","*.txt"),new FileChooser.ExtensionFilter("txt","*.TXT"));
        file = chooser.showOpenDialog(stage);
        if (file!=null)
            path.setText(file.getAbsolutePath());
    }

    @FXML
    private void testChapters(ActionEvent event){
        List<String> chapters = readTxt();
        ObservableList<String> items = FXCollections.observableList(chapters);

        list.setItems(items);

    }


    @FXML
    private void output(ActionEvent event){
        if (list.getItems().size() == 0){//没有切割章节，按每100行输出的规则切割
            startOutputPreLine();
            showSuccess2();
        }else {
            startOutput();
            showSuccess();
        }
    }

    private void showSuccess(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("输出成功");
        alert.setHeaderText(null);
        alert.setContentText("文件切割完成，请到txt目录下找同名文件夹");

        alert.showAndWait();
    }

    private void showSuccess2(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("输出成功");
        alert.setHeaderText(null);
        alert.setContentText("文件[没有匹配章节，按照行数切割]完成，请到txt目录下找同名文件夹");

        alert.showAndWait();
    }


    private List<String> readTxt(){
        if (file == null){
            return new ArrayList<>();
        }
        try {
            ArrayList<String> result = new ArrayList<>();
            FileInputStream inputStream = new FileInputStream(file);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,getCharset()));
            //正则
            String regexRule = regex.getText();
            String line = null;
            int counter = 1;
            while ((line = bufferedReader.readLine())!=null){
//                System.out.println(line);
                boolean isMatch = matchLine(line,regexRule);
                if (isMatch){
                    result.add(formatIndex(counter) +"-"+formatFileName(line));
                    counter += 1;
                }

            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }


    private void startOutputPreLine(){
        if (file == null){
            return ;
        }
        String bookName = file.getName().replaceAll(".txt","");
        File newDir = new File(file.getParentFile(),bookName);
        if (newDir.exists()) {
            newDir.delete();
        }
        newDir.mkdir();
        try {
            File currFile = new File(newDir,"00000-0000.txt");

            FileInputStream inputStream = new FileInputStream(file);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,getCharset()));

            FileOutputStream outputStream = new FileOutputStream(currFile);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
            //正则
            String line = null;
            int counter = 1;
            int lineCount = 0;
            while ((line = bufferedReader.readLine())!=null){
                lineCount ++;
//                System.out.println(line);
                boolean isMatch = lineCount >100;
                if (isMatch){
                    lineCount = 0;
                    writer.flush();
                    writer.close();
                    String newFileName = formatIndex(counter) +"-"+"x"+".txt";
                    currFile = new File(newDir,newFileName);
                    outputStream = new FileOutputStream(currFile);
                    writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
                    counter += 1;
                }
                writer.write(line);
                writer.write("\n");
                writer.flush();
            }
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        //压缩文件
        FileTOZip.fileToZip(newDir,new File(newDir.getParent(),bookName+".zip"),bookName);

    }


    private void startOutput(){
        if (file == null){
            return ;
        }
        String bookName = file.getName().replaceAll(".txt","").replaceAll(".TXT","");
        File newDir = new File(file.getParentFile(),bookName);
        if (newDir.exists()) {
            newDir.delete();
        }
        newDir.mkdir();
        try {
            File currFile = new File(newDir,"00000-0000.txt");

            FileInputStream inputStream = new FileInputStream(file);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,getCharset()));

            FileOutputStream outputStream = new FileOutputStream(currFile);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
            //正则
            String regexRule = regex.getText();
            String line = null;
            int counter = 1;
            while ((line = bufferedReader.readLine())!=null){
//                System.out.println(line);
                boolean isMatch = matchLine(line,regexRule);
                if (isMatch){
                    writer.flush();
                    writer.close();
                    String newFileName = formatIndex(counter) +"-"+formatFileName(line)+".txt";
                    currFile = new File(newDir,newFileName);
                    outputStream = new FileOutputStream(currFile);
                    writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
                    counter += 1;
                }
                writer.write(line);
                writer.write("\n");
                writer.flush();
            }
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        //压缩文件
        FileTOZip.fileToZip(newDir,new File(newDir.getParent(),bookName+".zip"),bookName);

    }

    private String formatIndex(int index){
        return String.format("%05d",index);
    }

    private String formatFileName(String name){
        return name.replaceAll("[\\/?*|\\\\<>\"':\t]","").replace((char) 12288,' ').trim();
    }

    private boolean matchLine(String line,String regexRule){
        Pattern t = Pattern.compile(regexRule);
        Matcher tt = t.matcher(line);
        return tt.find();
    }

    public String getCharset() {
        EncodingDetect s = new EncodingDetect();
        return EncodingDetect.javaname[s.detectEncoding(file)];
    }


}
