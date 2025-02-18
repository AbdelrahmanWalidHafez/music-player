import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MusicPlayList extends JDialog {
    private final ArrayList<String >songPaths;//stores all the paths of the songs to be written in a file
    public  MusicPlayList(MusicPlayerGui musicPlayerGui){
        songPaths=new ArrayList<>();
        setTitle("Create PlayList");
        setSize(400,400);
        setResizable(false);
        getContentPane().setBackground(MusicPlayerGui.FRAMECOLOR);
        setLayout(null);
        setModal(true);//this makes the dialog has to be closed to give focus on the musicPlayerGui
        setLocationRelativeTo(musicPlayerGui);
        addComponents();
    }
    private void addComponents(){
        //container to add each song path
        JPanel songContainer=new JPanel();
        songContainer.setLayout(new BoxLayout(songContainer,BoxLayout.Y_AXIS));
        songContainer.setBounds((int)(getWidth()*0.025),10,(int)(getWidth()*0.90),(int)(getHeight()*0.75));
        add(songContainer);
        JButton addSong =new JButton("Add");
        addSong.setBounds(60,(int)(getHeight()*0.80),100,25);
        addSong.setFont(new Font("Dialog",Font.BOLD,14));
        addSong.addActionListener(e -> {
            JFileChooser fileChooser=new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("MP3","mp3"));
            fileChooser.setCurrentDirectory(new File("src/assets"));
            int flag=fileChooser.showOpenDialog(MusicPlayList.this);
            File selectedFile=fileChooser.getSelectedFile();
            if(flag== JFileChooser.APPROVE_OPTION &&selectedFile!=null){
                JLabel songPath=new JLabel(selectedFile.getPath());
                songPath.setFont(new Font("Dialog",Font.BOLD,12));
                songPath.setBorder(BorderFactory.createLineBorder(MusicPlayerGui.FRAMECOLOR));
                songPaths.add(songPath.getText());//add to the list
                songContainer.add(songPath);
                songContainer.revalidate();//refresh the dialogue after the label has been added
            }
        });
        add(addSong);
        JButton savePlaylist=new JButton("Save");
        savePlaylist.setBounds(215,(int)(getHeight()*0.80),100,25);
        savePlaylist.setFont(new Font("Dialog",Font.BOLD,14));
        savePlaylist.addActionListener(e -> {
            JFileChooser fileChooser=new JFileChooser();
            fileChooser.setCurrentDirectory(new File("src/assets"));
            int flag=fileChooser.showSaveDialog(MusicPlayList.this);
            if(flag==JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();// the reference of the mp3 file
                //checks that the file is not a txt file if the file is not converted already
                if (!selectedFile.getName().substring(selectedFile.getName().length() - 4).equalsIgnoreCase(".txt")) {//gets the extension of the file
                    selectedFile = new File(selectedFile.getAbsoluteFile() + ".txt");
                }
                try {
                    selectedFile.createNewFile();// create new file to store the paths for each playlist
                    FileWriter fileWriter = new FileWriter(selectedFile);
                    for (String songPath : songPaths) {
                        fileWriter.write(songPath + "\n");
                    }
                    fileWriter.close();
                    JOptionPane.showMessageDialog(MusicPlayList.this,"PlayList created !");
                    this.dispose();//close the dialogue
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        add(savePlaylist);


    }
}
