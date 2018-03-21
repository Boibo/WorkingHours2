package bobthebraino.workinghours;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;
import java.math.BigDecimal;
import java.util.List;
import bobthebraino.database.DatabaseConnector;
import bobthebraino.database.tables.LoginStatus;
import bobthebraino.database.tables.Timestamp;

/**
 * The app`s main activity
 */
public class MainActivity extends Activity {
    private boolean LoggedIn = false;
    private TextView StatusView;
    private TextView TimeView;
    private DatabaseConnector DBConnection;
    private List Timestamps;

    @Override
    /**
     * OnCreate: Set the current contentView, call the intitialization method
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       init();
    }

    /**
     * Initialize the app
     */
    private void init() {
        DBConnection = new DatabaseConnector(getApplicationContext());
        DBConnection.onCreate(DBConnection.getWritableDatabase());

        StatusView = (TextView)findViewById(R.id.statusView);
        TimeView = (TextView)findViewById(R.id.timeView);
        TextView tv = (TextView) findViewById(R.id.timestamps);
        tv.setMovementMethod(new ScrollingMovementMethod());

        List<LoginStatus> list = DBConnection.LoadAll(LoginStatus.class);
        if(list.size() == 0)
            DBConnection.Insert(new LoginStatus(DBConnection, false));
        LoggedIn = list.size() > 0 ? list.get(0).LoggedIn : false;
        Timestamps = DBConnection.LoadAll(Timestamp.class);

        String timestampTXT = "";
        for(int i = 0; i < Timestamps.size(); i++) {
            Timestamp t = (Timestamp)Timestamps.get(i);
            timestampTXT += t.Login ? "Anw: " : "Abw: ";
            timestampTXT += (t.Hour > 9 ? t.Hour : "0" + t.Hour) + ":" + (t.Minute > 9 ? t.Minute : "0" + t.Minute) + "\n";
        }

        tv.setText(timestampTXT);
        TimeView.setText(calcTodaysWorkingTime());
        StatusView.setText(LoggedIn ? "Anwesend" : "Abwesend");
    }

    /**
     * UI command: take a new timestamp
     * @param _view
     */
    public void takeTimestamp(View _view) {
        if (LoggedIn) {
            StatusView.setText("Abwesend");
            LoggedIn = false;
        } else {
            StatusView.setText("Anwesend");
            LoggedIn = true;
        }
        Timestamp t = new Timestamp(DBConnection, LoggedIn);
        DBConnection.Insert(t);
        Timestamps.add(t);
        LoginStatus loginStatus = DBConnection.LoadAll(LoginStatus.class).get(0);
        loginStatus.LoggedIn = LoggedIn;
        DBConnection.Update(loginStatus);

        TextView tv = (TextView)findViewById(R.id.timestamps);
        String timestamps = tv.getText().toString();
        timestamps += t.Login ? "Anw: " : "Abw: ";
        timestamps += (t.Hour > 9 ? t.Hour : "0" + t.Hour) + ":" + (t.Minute > 9 ? t.Minute : "0" + t.Minute) + "\n";
        TimeView.setText(calcTodaysWorkingTime());

        tv.setText(timestamps);
    }

    /**
     * UI command for debug purposes: drop the current database to clean it
     * @param _view
     */
    public void dropDatabase(View _view){
        DBConnection.dropDatabase();
        init();
    }

    /**
     * Calculates todays current working time
     * @return
     */
    private String calcTodaysWorkingTime() {
        if(Timestamps.size() > 1 && ((Timestamp)Timestamps.get(0)).Login) {
            long timeWorkedS = 0;
            for(int i = 0; i < Timestamps.size() - 1; i+=2) {
                if(i % 2 == 0) {
                    if (((Timestamp) Timestamps.get(i)).Login &&
                            !((Timestamp) Timestamps.get(i + 1)).Login) {
                        timeWorkedS += (((Timestamp) Timestamps.get(i + 1)).Millisecondstotal - ((Timestamp) Timestamps.get(i)).Millisecondstotal) / 1000;
                    }
                }
            }

            BigDecimal secondsTotal = new BigDecimal(timeWorkedS);
            BigDecimal hours = secondsTotal.divide(new BigDecimal(3600), BigDecimal.ROUND_FLOOR);
            BigDecimal myremainder = secondsTotal.remainder(new BigDecimal(3600));
            BigDecimal minutes = myremainder.divide(new BigDecimal(60), BigDecimal.ROUND_FLOOR);
            BigDecimal seconds = myremainder.remainder(new BigDecimal(60));
            return (hours.intValue() > 9 ? hours : "0" + hours.toString()) + ":"
                    + (minutes.intValue() > 9 ? minutes : "0" + minutes.toString()) + ":"
                    + (seconds.intValue() > 9 ? seconds : "0" + seconds.toString());
        }
        return "00:00:00";
    }
}
