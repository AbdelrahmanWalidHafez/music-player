import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Hashtable;

public class MusicPlayerGui extends JFrame {
    public static  final Color FRAMECOLOR=Color.BLACK;
    public static final  Color TEXTCOLOR=Color.white;
    private final   MusicPlayer musicPlayer;
    private final JFileChooser fileChooser;
    private  JLabel songArtist;
    private JLabel songTitle;
    private JPanel playButtons;
    JSlider playBackSlider;
    public MusicPlayerGui(){
        super("Music Player");//title
        setSize(400,600);//set width and height of the frame
        setDefaultCloseOperation(EXIT_ON_CLOSE);//when user press on the x button the music player closes
        setLocationRelativeTo(null);//to centre the app on the screen
        setResizable(false);//to not be able to resize
        setLayout(null);//to be able to control the coordinates of our gui components
        getContentPane().setBackground(FRAMECOLOR);//change the color of the frame
        musicPlayer=new MusicPlayer(this);
        fileChooser=new JFileChooser();
        fileChooser.setCurrentDirectory(new File("src/assets"));//set the default path to the file explorer
        fileChooser.setFileFilter(new FileNameExtensionFilter("MP3","mp3"));//to be able to see only the mp3 files
        addGuiComponents();
    }
    private void addGuiComponents(){
        addToolBar();//adding the toolbar
        //load the song images
        JLabel songImage=new JLabel(loadImage("src/assets/record.png"));
        songImage.setBounds(0,50,getWidth()-20,225);
        add(songImage);
        //song title
        songTitle = new JLabel("Song Title");
        songTitle.setBounds(0,285,getWidth()-10,30);
        songTitle.setFont(new Font("Arial",Font.BOLD,24));
        songTitle.setForeground(TEXTCOLOR);
        songTitle.setHorizontalAlignment(SwingConstants.CENTER);
        add(songTitle);
        //song artist
        songArtist=new JLabel("Artist");
        songArtist.setBounds(0,315,getWidth()-10,30);
        songArtist.setFont(new Font("Dialog",Font.PLAIN,24));
        songArtist.setForeground(TEXTCOLOR);
        songArtist.setHorizontalAlignment(SwingConstants.CENTER);
        add(songArtist);
        //playback slider
        playBackSlider=new JSlider(JSlider.HORIZONTAL,0,100,0);
        playBackSlider.setBounds(( getWidth()/ 2) - 150,365,300,40);
        playBackSlider.setBackground(null);
        playBackSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                //when user is holding the slider when want to pause the music until he releases the slider
                musicPlayer.pauseSong();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
              //when the user releases the slider
               musicPlayer.setCurrentFrame( ((JSlider) e.getSource()).getValue());//the frame where the user wants to restart playing the music from it
                //update the current time in milliseconds
                musicPlayer.setCurrentTimeInMilliseconds((int)((((JSlider) e.getSource()).getValue())/(2.08*musicPlayer.getCurrentSong().getFrameRatePerMilliseconds())));
                musicPlayer.playCurrentSong();//resume the song
                enablePauseButtonDisablePlayButton();
            }
        });
        add(playBackSlider);
        //play buttons
        addPlayButtons();
    }
    private void addToolBar(){
        JToolBar toolBar=new JToolBar();
        toolBar.setBounds(0,0,getWidth(),20);
        toolBar.setFloatable(false);//to prevent the toolbar from being moved
        JMenuBar menuBar=new JMenuBar();
        toolBar.add(menuBar);//adding the menu bar to the toolbar
        JMenu songMenu=new JMenu("Song");
        menuBar.add(songMenu);//adding the song menu to the menu bar
        JMenuItem loadSong=new JMenuItem("Load song");
        loadSong.addActionListener(e -> {
            //an integer to let us know what the user did
            int flag=fileChooser.showOpenDialog(MusicPlayerGui.this);
            File selectedFile=fileChooser.getSelectedFile();
            //we check if the user also pressed the open button
            if(selectedFile!=null &&flag== JFileChooser.APPROVE_OPTION){
                //create a song object
                Song song=new Song(selectedFile.getPath());
                //load son in music player
                musicPlayer.loadSong(song);
                //update the song title and artist
                updateSongInfo(song);
                //toggle on pause button and toggle off play button
                enablePauseButtonDisablePlayButton();
                //update the slider
                updatePlayBackSlider(song);
            }
        });
        songMenu.add(loadSong);//adding the load song option to the song menu bar
        JMenu playListMenu=new JMenu("Play list");
        menuBar.add(playListMenu);//adding the play list menu to the menu bar
        JMenuItem createPlayList=new JMenuItem("Create Playlist");
        createPlayList.addActionListener(e -> {
            //load music playList dialog
            new MusicPlayList(MusicPlayerGui.this).setVisible(true);

        });
        playListMenu.add(createPlayList);//adding the creation of a playlist option to the playlist menu
        JMenuItem loadPlayList=new JMenuItem("Load PlayList");
        loadPlayList.addActionListener(e->{
            JFileChooser jFileChooser=new JFileChooser();
            jFileChooser.setFileFilter(new FileNameExtensionFilter("PlayList","txt"));
            jFileChooser.setCurrentDirectory(new File("src/assets"));
            int flag=jFileChooser.showOpenDialog(MusicPlayerGui.this);
            File selectedFile=jFileChooser.getSelectedFile();
            if(flag==JFileChooser.APPROVE_OPTION&&selectedFile!=null){
                musicPlayer.stopSong();//stop the music
                musicPlayer.loadPlayList(selectedFile);

            }
        });
        playListMenu.add(loadPlayList);//same as the creation of a playlist
        add(toolBar);
    }
    private ImageIcon loadImage(String imagePath){
        try{
            // read the image file from the givenPath
            BufferedImage image= ImageIO.read(new File(imagePath));
            return new ImageIcon(image);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    private void  addPlayButtons(){
         playButtons=new JPanel();
        playButtons.setBounds(0,435,getWidth()-10,80);
        playButtons.setBackground(null);
        JButton prev= new JButton(loadImage("src/assets/previous.png"));
        prev.setBorderPainted(false);
        prev.setBackground(null);
        prev.addActionListener(e-> musicPlayer.playPreviousSong());
        playButtons.add(prev);
        JButton play= new JButton(loadImage("src/assets/play.png"));
        play.setBorderPainted(false);
        play.setBackground(null);
        play.addActionListener(e->{
            enablePauseButtonDisablePlayButton();
            musicPlayer.playCurrentSong();
        });
        playButtons.add(play);
        JButton pause= new JButton(loadImage("src/assets/pause.png"));
        pause.setBorderPainted(false);
        pause.setBackground(null);
        pause.setVisible(false);
        pause.addActionListener(e -> {
            enablePlayButtonDisablePauseButton();
            musicPlayer.pauseSong();
        });
        playButtons.add(pause);
        JButton next= new JButton(loadImage("src/assets/next.png"));
        next.setBorderPainted(false);
        next.setBackground(null);
        next.addActionListener(e-> musicPlayer.playNextSong());
        playButtons.add(next);
        add(playButtons);
    }
    public void updateSongInfo(Song song){
        songTitle.setText(song.getSongTitle());
        songArtist.setText(song.getSongArtist());
    }
    public void enablePauseButtonDisablePlayButton(){
        //retrive reference to play button from playButtons
        JButton playButton=(JButton)playButtons.getComponent(1);
        JButton pauseButton=(JButton)playButtons.getComponent(2);
        //turn off play button
        playButton.setVisible(false);
        playButton.setEnabled(false);
        pauseButton.setVisible(true);
        pauseButton.setEnabled(true);
    }
    public void enablePlayButtonDisablePauseButton(){
        //retrive reference to play button from playButtons
        JButton playButton=(JButton)playButtons.getComponent(1);
        JButton pauseButton=(JButton)playButtons.getComponent(2);
        //turn on play button
        playButton.setVisible(true);
        playButton.setEnabled(true);
        pauseButton.setVisible(false);
        pauseButton.setEnabled(false);
    }
    public void updatePlayBackSlider(Song song){
        //update the max count of the slider
        playBackSlider.setMaximum(song.getMp3File().getFrameCount());
        //create the song length label
        Hashtable<Integer,JLabel>labelTable=new Hashtable<>();
        //we begin from 00:00
        JLabel beginning =new JLabel("00:00");
        beginning.setFont(new Font("Dialog",Font.BOLD,18));
        beginning.setForeground(TEXTCOLOR);
        JLabel end=new JLabel(song.getSongLength());
        end.setFont(new Font("Dialog",Font.BOLD,18));
        end.setForeground(TEXTCOLOR);
        labelTable.put(0,beginning);
        labelTable.put(song.getMp3File().getFrameCount(),end);
        playBackSlider.setLabelTable(labelTable);
        playBackSlider.setPaintLabels(true);
    }
    //used to update our slider from the music player class
    public void setPlayBackSliderValue(int frame){//not used yet
        playBackSlider.setValue(frame);
    }
}
