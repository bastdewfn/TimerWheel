package timerwheel.autoconfigure;

import com.dewfn.timerwheel.ITimerWheel;
import com.dewfn.timerwheel.TimerWheel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass({TimerWheel.class})
public class TimerWheelAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public ITimerWheel timerWheel(){
        ITimerWheel timerWheel=new TimerWheel();
        return timerWheel;
    }
}
