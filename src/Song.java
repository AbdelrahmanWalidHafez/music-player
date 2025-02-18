import com.mpatric.mp3agic.Mp3File;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import java.io.File;
//class that have the song details
public class Song {
    private String songTitle;
    private String songArtist;
    private String songLength;
    private final String songPath;
    private Mp3File mp3File;
    private double frameRatePerMilliseconds;
    public Song(String songPath){
        this.songPath=songPath;
        try{
            mp3File=new Mp3File(songPath);
            frameRatePerMilliseconds=(double)mp3File.getFrameCount()/mp3File.getLengthInMilliseconds();
            songLength=convertToSongLengthFormat();
            AudioFile audioFile= AudioFileIO.read(new File(songPath));//using the jaudiotagger library to create an audio file object to read mp3 files
            //reading the details of the songs through its metadata
            Tag tag=audioFile.getTag();
            if(tag!=null){
                songTitle=tag.getFirst(FieldKey.TITLE);
                songArtist=tag.getFirst(FieldKey.ARTIST);
            }else{
                songTitle="N/A";
                songArtist="N/A";
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private String convertToSongLengthFormat(){
        long minutes=mp3File.getLengthInSeconds()/60;
        long seconds=mp3File.getLengthInMilliseconds()%60;
        return String.format("%02d:%02d",minutes,seconds);
    }
    public String getSongTitle() {
        return songTitle;
    }
    public String getSongArtist() {
        return songArtist;
    }

    public String getSongLength() {
        return songLength;
    }

    public String getSongPath() {
        return songPath;
    }

    public Mp3File getMp3File() {
        return mp3File;
    }

    public double getFrameRatePerMilliseconds() {
        return frameRatePerMilliseconds;
    }
}
