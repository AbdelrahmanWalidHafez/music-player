import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class MusicPlayer extends PlaybackListener {
    //this will be used to update isPaused more synchronously
    private static final Object playSignal=new Object();
    private final MusicPlayerGui musicPlayerGui;
    private Song currentSong;
    // use Jlayer library to create an advanced player object which will handle playing the music
    private AdvancedPlayer advancedPlayer;
    private boolean isPaused;//used to check whether the song is paused or not
    private int currentFrame;//is used to get the frame where the song has been paused at milliseconds
    private ArrayList<Song> playList;
    private int playListIndex;
    private int currentTimeInMilliseconds;//track how many milliseconds has passed
    private  boolean songFinished;  /*flag to fix the bug that arise when the song finished we call the stop function
      but the song is already stopped which reflect on the next song feature */
    private boolean isNextPressed,isPrevPressed;//used to make sure that the next/prev btns not pressed when the function playback has called
    public MusicPlayer(MusicPlayerGui musicPlayerGui){
        this.musicPlayerGui=musicPlayerGui;
    }
    public void loadSong(Song song){
        currentSong=song;
        playList=null;//if we loaded a song this means that we are not using the playList anymore
        if(!songFinished) stopSong();//to prevent two songs to play at the same time when we load a song and then play a song from a playlist
        if(currentSong!=null){
            //to prevent that when we are playing a song and load another song the slider will not be reseted
            currentFrame=0;
            currentTimeInMilliseconds=0;
            musicPlayerGui.setPlayBackSliderValue(0);
            playCurrentSong();
        }
    }
    public void playCurrentSong(){
        if(currentSong==null){//to avoids the exception when the song is paused and resumed
            return;
        }
        try{
            //read mp3 audio data
            FileInputStream fileInputStream=new FileInputStream(currentSong.getSongPath());
            BufferedInputStream bufferedInputStream=new BufferedInputStream(fileInputStream);
            //create advanced player
            advancedPlayer=new AdvancedPlayer(bufferedInputStream);
            advancedPlayer.setPlayBackListener(this);
            //start music thread
            startMusicThread();
            //start the playback slider thread
            startPlayBackSliderThread();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    // will handle the playing  music
    private void startMusicThread(){
        new Thread(() -> {
            try{
                if(isPaused){
                    synchronized (playSignal) {
                        isPaused = false;
                        playSignal.notify();//notify the other threads to continue and makes sure that the flag has been updated correctly
                    }
                    //resume the music from where its paused
                    advancedPlayer.play(currentFrame,Integer.MAX_VALUE);
                }else {
                    //play the music from the beginning
                    advancedPlayer.play();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }
    public void pauseSong(){
        if(advancedPlayer!=null){
            isPaused=true;
            stopSong();
        }
    }
    public void stopSong(){
        if(advancedPlayer!=null){
            advancedPlayer.stop();
            advancedPlayer.close();
            advancedPlayer=null;
        }
    }
//this method is called at the beginning of the song
    @Override
    public void playbackStarted(PlaybackEvent evt) {
        songFinished=false;//update
        isNextPressed=false;
        isPrevPressed=false;
    }
//this function is called at the end of the song
    @Override
    public void playbackFinished(PlaybackEvent evt) {
        songFinished=true;//update
        if(isPaused){
            currentFrame+=(int) ((double)evt.getFrame()*currentSong.getFrameRatePerMilliseconds());// we used += cause the frame will be rested
            //when the song is finished plays the next song
        }else {
            if(isPrevPressed||isNextPressed){// if the user pressed the next or prev button then there will no need to goto next song
               return;
            }
           if(playList==null){
               musicPlayerGui.enablePlayButtonDisablePauseButton();
           }else {
               if(playListIndex==playList.size()-1){
                   musicPlayerGui.enablePlayButtonDisablePauseButton();
               }else {
                   playNextSong();
               }
           }
           }
        }
    public void setCurrentFrame(int currentFrame){
        this.currentFrame=currentFrame;
    }
    public  void  setCurrentTimeInMilliseconds(int currentTimeInMilliseconds){
        this.currentTimeInMilliseconds=currentTimeInMilliseconds;
    }
    public Song getCurrentSong() {
        return currentSong;
    }
    public void loadPlayList(File playListFile){
        playList=new ArrayList<>();
        try{
            Scanner scanner= new Scanner(playListFile);
            while (scanner.hasNext()){
                Song song=new Song(scanner.nextLine());
                playList.add(song);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        if(!playList.isEmpty()){
            musicPlayerGui.setPlayBackSliderValue(0);//reset the slider to the beginning
            currentTimeInMilliseconds=0;
            currentSong=playList.getFirst();//set the current song to be played the first song in the play list
            currentFrame=0;//start from the beginning of the song
            //update the gui
            musicPlayerGui.enablePauseButtonDisablePlayButton();
            musicPlayerGui.updateSongInfo(currentSong);
            musicPlayerGui.updatePlayBackSlider(currentSong);
            playCurrentSong();

        }
    }
    public void playNextSong(){
        if(playList==null){
            return;
        }
        if(playListIndex+1>playList.size()-1){//to check we are at the end of the list or not
            playListIndex=-1;
        }
        isNextPressed=true;
        if(!songFinished) stopSong();
        currentSong=playList.get(++playListIndex);
        currentTimeInMilliseconds=0;
        currentFrame=0;
        musicPlayerGui.enablePauseButtonDisablePlayButton();
        musicPlayerGui.updateSongInfo(currentSong);
        musicPlayerGui.updatePlayBackSlider(currentSong);
        playCurrentSong();
    }
    public void playPreviousSong(){
        if(playList==null){
            return;
        }
        if(playListIndex-1<0){
            playListIndex=playList.size();
        }
        isPrevPressed=true;
        if(!songFinished) stopSong();
        currentSong=playList.get(--playListIndex);
        currentTimeInMilliseconds=0;
        currentFrame=0;
        musicPlayerGui.enablePauseButtonDisablePlayButton();
        musicPlayerGui.updateSongInfo(currentSong);
        musicPlayerGui.updatePlayBackSlider(currentSong);
        playCurrentSong();
    }
    //thread that will handle updating the slider
    private void startPlayBackSliderThread(){
        new Thread(() -> {
            if(isPaused){
                //wait till it gets a notify by the other thread
                try{
                    playSignal.wait();
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
            while (!isPaused&&!songFinished&&!isNextPressed&&!isPrevPressed){/* we will check the other conditions to prevent that multiple threads from updating
             the slider incrementation */
                currentTimeInMilliseconds++;
                int calculatedFrame=(int)((double)currentTimeInMilliseconds*2.08*currentSong.getFrameRatePerMilliseconds());

                //update the gui
                musicPlayerGui.setPlayBackSliderValue(calculatedFrame);
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    /*
    there is problem that the method get frame gives us the frames in milliseconds which is not precise, so we must get
    the Frame by evt.getFrame x totalFrames/ms(songLength
     */
}
