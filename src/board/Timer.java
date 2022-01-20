package board;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Timer{
    private long m_incrementValue;
    JLabel m_timeLabel = new JLabel();
    private int m_seconds;
    private int m_minutes;
    private int m_miliseconds;
    String m_secondsString = String.format("%02d", m_seconds);
    private String m_minutesString = String.format("%02d", m_minutes);
    private boolean m_hasFinished;

    public Timer(int timePerSide, int incrementPerMove){
        setNewValues(timePerSide, incrementPerMove);

        m_timeLabel.setText(m_minutesString + ":" + m_secondsString);
        m_timeLabel.setFont(new Font("Calibri", Font.PLAIN, 22));
        m_timeLabel.setBackground(new Color(0x2c2723));
        m_timeLabel.setForeground(new Color(0x83807e));
        m_timeLabel.setMaximumSize(new Dimension(250, m_timeLabel.getPreferredSize().height));
        m_timeLabel.setOpaque(true);
        m_timeLabel.setVisible(true);
    }

    javax.swing.Timer m_timer = new javax.swing.Timer(100, new ActionListener(){
        @Override
        public void actionPerformed(ActionEvent e){
            m_miliseconds -= 100;
            if(m_miliseconds < 0){
                m_miliseconds += 1000;
                --m_seconds;
            }

            if(m_seconds < 0){
                m_seconds = 59;
                --m_minutes;
            }

            if(m_minutes < 0){
                m_hasFinished = true;
                stop();
                m_minutesString = "00";
                m_secondsString = "00";
                m_timeLabel.setText(m_minutesString + ":" + m_secondsString);
                return;
            }

            m_minutesString = String.format("%02d", m_minutes);
            m_secondsString = String.format("%02d", m_seconds);
            m_timeLabel.setText(m_minutesString + ":" + m_secondsString);
        }
    });

    public JLabel getTimeLabel(){return m_timeLabel;}

    public void setNewValues(int newStartValue, int newIncrementValue){
        m_timer.stop();
        m_incrementValue = newIncrementValue;
        m_seconds = 0;
        m_miliseconds = 0;
        m_minutes = newStartValue;
        m_hasFinished = false;
    }

    public void start(){
        m_minutesString = String.format("%02d", m_minutes);
        m_secondsString = String.format("%02d", m_seconds);
        m_timeLabel.setText(m_minutesString + ":" + m_secondsString);
        m_timer.start();
    }

    public void stop(){
        m_timer.stop();
    }

    public boolean hasFinished(){
        return m_hasFinished;
    }

    public void increment(){
        m_seconds += m_incrementValue;
        if(m_seconds >= 60){
            m_seconds -= 60;
            m_minutes += 1;
        }

        m_minutesString = String.format("%02d", m_minutes);
        m_secondsString = String.format("%02d", m_seconds);
        m_timeLabel.setText(m_minutesString + ":" + m_secondsString);
    }

    public boolean isRunning(){return m_timer.isRunning();}
}
