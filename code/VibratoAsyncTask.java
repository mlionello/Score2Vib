import android.media.SoundPool;
import android.os.AsyncTask;

public class VibratoAsyncTask extends AsyncTask<Object, Integer, Void> {
    // Member variables
    private SoundPool sndPool1; // SoundPool for playing metronome ticks
    private boolean start = false; // Flag indicating if the application has started
    private long startTime; // Start time of the application
    private long nextOnset; // Time of the next note onset
    private long onsetTime; // Time of the current note onset
    private long tot_dur; // Total duration since the start
    private int beat = 0; // Current beat index
    private boolean first = true; // Flag indicating the first beat
    private long movingWinTime = 0; // Time for scrolling prediction view
    private int counter = 0; // Counter for pitch drawing
    private int ticks; // Sound ID for metronome ticks
    private int tempo; // Tempo of the score

    // Constructor
    protected VibratoAsyncTask(SoundPool soundPool, int ticksSoundId, int tempo) {
        this.sndPool1 = soundPool;
        this.ticks = ticksSoundId;
        this.tempo = tempo;
    }

    // Background task
    protected Object doInBackground(Object[] params) {
        Long quarterTime = (long) 0; // Time for metronome ticks
        while (true) {
            // Check if the application should stop
            if (VibratoActivity.stop)
                break;

            // Check if the application should start
            if (!VibratoActivity.start)
                continue;

            // Perform initialization tasks when starting
            if (VibratoActivity.start && !start) {
                // Play metronome ticks to indicate tempo
                for (int i = 0; i < 4; i++) {
                    sndPool1.play(ticks, 1f, 1f, 1, 0, 1f);
                    Thread.sleep(((60000 / VibratoActivity.tempo)) * 1000000);
                }
                start = true;
                // Notify UI to update
                publishProgress(-2);
                startTime = System.currentTimeMillis();
            }

            // Wait for the next beat and update metronome ticks
            while ((System.currentTimeMillis() - startTime) == 0)
                continue;

            // Update metronome ticks based on tempo
            if ((((System.currentTimeMillis() - startTime) / 1000.0) % (60.0 / tempo)) == 0 && System.currentTimeMillis() - quarterTime > 10) {
                quarterTime = System.currentTimeMillis();
                publishProgress(-2);
            }

            // Update UI for new notes
            if (System.currentTimeMillis() > nextOnset) {
                onsetTime = System.currentTimeMillis();
                tot_dur = onsetTime - startTime;
                nextOnset = onsetTime + (long) (VibratoActivity.predictions[this.beat + 1][2] * (60 / tempo) * 1000);
                publishProgress(-1);
                // Check if reached end of score
                if (this.beat == VibratoActivity.score_length)
                    VibratoActivity.stop = true;
                // Skip first beat to synchronize with score
                if (first){
                    first=false;
                    continue;
                }
                this.beat++;
            }

            // Scroll the prediction view every 50ms
            if (System.currentTimeMillis() - movingWinTime > 50 ) {
                movingWinTime = System.currentTimeMillis();
                publishProgress(-3);
            }
            // Update UI for pitch drawing
            if (IAudioTask.counter != this.counter) {
                publishProgress(-4);
                this.counter = IAudioTask.counter;
            }
        }
        return null;
    }

    // UI update method
    protected void onProgressUpdate(Integer... pos) {
        switch (pos[0]) {
            case -2:
                // Play metronome ticks
                sndPool1.play(ticks, 1f, 1f, 1, 0, 1f);
                break;
            case -3:
                // Draw predictions
                VibratoActivity.vibgi.drawPredictions(0);
                VibratoActivity.vibgi.drawTest(0);
                break;
            case -4:
                // Draw pitch
                VibratoActivity.vibgi.drawPitch(0);
                break;
        }
    }
}
