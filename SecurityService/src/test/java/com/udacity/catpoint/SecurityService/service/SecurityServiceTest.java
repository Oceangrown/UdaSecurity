package com.udacity.catpoint.SecurityService.service;

import com.udacity.catpoint.ImageService.FakeImageService;
import com.udacity.catpoint.ImageService.ImageService;
import com.udacity.catpoint.SecurityService.data.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;

public class SecurityServiceTest {
    @Mock
    private ImageService imageService;
    @Mock
    private SecurityRepository securityRepository;
    private SecurityService securityService;
    @BeforeEach
    void setUp(){
        securityRepository = Mockito.mock(SecurityRepository.class);
        imageService = Mockito.mock(ImageService.class);
        securityService = new SecurityService(securityRepository, imageService);
    }
    // If the system is disarmed, set the status to no alarm.
    // 9
    @Test
    void testSetArmingStatus01(){
        //https://stackoverflow.com/questions/9841623/mockito-how-to-verify-method-was-called-on-an-object-created-within-a-method
    securityService.setArmingStatus(ArmingStatus.DISARMED);
    verify(securityRepository, times(1)).setArmingStatus(ArmingStatus.DISARMED);
    verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);

    }
    // If the system is armed, reset all sensors to inactive.
    // 10
    @Test
    void testSetArmingStatus02(){
        Sensor sensor1 = new Sensor("testSensor1", SensorType.MOTION);
        Sensor sensor2 = new Sensor("testSensor2", SensorType.DOOR);
        sensor1.setActive(true);
        sensor2.setActive(true);
        Set<Sensor> sensors = new HashSet<>();
        sensors.add(sensor1);
        sensors.add(sensor2);
        when(securityRepository.getSensors()).thenReturn(sensors);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        securityService.setArmingStatus(ArmingStatus.ARMED_AWAY);
        assertFalse(sensor1.getActive());
        assertFalse(sensor2.getActive());
    }
    // If the system is armed, reset all sensors to inactive.
    // 10
    @Test
    void testSetArmingStatus03(){
        Sensor sensor1 = new Sensor("testSensor1", SensorType.MOTION);
        Sensor sensor2 = new Sensor("testSensor2", SensorType.DOOR);
        sensor1.setActive(true);
        sensor2.setActive(true);
        Set<Sensor> sensors = new HashSet<>();
        sensors.add(sensor1);
        sensors.add(sensor2);
        when(securityRepository.getSensors()).thenReturn(sensors);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        assertFalse(sensor1.getActive());
        assertFalse(sensor2.getActive());
    }
    @Test
    void testChangeSensorActivationStatus01(){
        Sensor sensor = new Sensor("testSensor", SensorType.DOOR);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);
        securityService.changeSensorActivationStatus(sensor, true);
        assertTrue(sensor.getActive());
    }

    // If alarm is armed and a sensor becomes activated, put the system into pending alarm status.
    // 1
    @Test
    void testChangeSensorActivationStatus02(){
        Sensor sensor = new Sensor("testSensor", SensorType.DOOR);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_AWAY);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        securityService.changeSensorActivationStatus(sensor, true);
        verify(securityRepository).setAlarmStatus(AlarmStatus.PENDING_ALARM);
        assertTrue(sensor.getActive());
    }
    // If alarm is armed and a sensor becomes activated and the system is already pending alarm, set off the alarm.
    // 2
    @Test
    void testChangeSensorActivationStatus03(){
        Sensor sensor = new Sensor("testSensor", SensorType.DOOR);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor, true);
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
        assertTrue(sensor.getActive());
    }
    // If pending alarm and all sensors are inactive, return to no alarm state
    // 3
    @Test
    void testChangeSensorActivationStatus04(){
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        Sensor sensor1 = new Sensor("testSensor1", SensorType.DOOR);
        Sensor sensor2 = new Sensor("testSensor2", SensorType.WINDOW);
        sensor1.setActive(true);
        sensor2.setActive(true);
        Set<Sensor> sensors = new HashSet<>();
        sensors.add(sensor1);
        sensors.add(sensor2);
        when(securityRepository.getSensors()).thenReturn(sensors);
        // deactivating sensor1, should not change the alarm status into no alarm because other active sensors
        securityService.changeSensorActivationStatus(sensor1, false);
        verify(securityRepository, times(0)).setAlarmStatus(AlarmStatus.NO_ALARM);
        // deactivating last active sensor, should change alarm status to no alarm
        securityService.changeSensorActivationStatus(sensor2, false);
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }
    // If alarm is active, change in sensor state should not affect the alarm state.
    // 4
    @Test
    void testChangeSensorActivationStatus05(){
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        Sensor sensor1 = new Sensor("testSensor1", SensorType.MOTION);
        securityService.changeSensorActivationStatus(sensor1, true);
        verify(securityRepository, times(0)).setAlarmStatus(AlarmStatus.PENDING_ALARM);
        verify(securityRepository, times(0)).setAlarmStatus(AlarmStatus.NO_ALARM);
        securityService.changeSensorActivationStatus(sensor1, false);
        verify(securityRepository, times(0)).setAlarmStatus(AlarmStatus.NO_ALARM);
        verify(securityRepository, times(0)).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }
    // If a sensor is activated while already active and the system is in pending state, change it to alarm state.
    // 5
    @Test
    void testChangeSensorActivationStatus06(){
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        Sensor sensor1 = new Sensor("testSensor", SensorType.DOOR);
        sensor1.setActive(true);
        securityService.changeSensorActivationStatus(sensor1, true);
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }
    // If a sensor is deactivated while already inactive, make no changes to the alarm state.
    // 6
    @Test
    void testChangeSensorActivationStatus07(){
       Sensor sensor1 = new Sensor("testSensor", SensorType.WINDOW);
       sensor1.setActive(false);
       securityService.changeSensorActivationStatus(sensor1, false);
       verify(securityRepository, times(0)).setAlarmStatus(any());
    }
    // If the camera image contains a cat while the system is armed-home, put the system into alarm status.
    // 7
    @Test
    void testImageProcess01(){
            BufferedImage image1 = new BufferedImage(300, 400, BufferedImage.TYPE_INT_RGB);
            when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);
            when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
            securityService.processImage(image1);
            verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }
    // If the camera image does not contain a cat, change the status to no alarm as long as the sensors are not active.
    // 8
    @Test
    void testImageProcess02(){
        BufferedImage image1 = new BufferedImage(300, 400, BufferedImage.TYPE_INT_RGB);
        Sensor sensor1 = new Sensor("testSensor1", SensorType.MOTION);
        Sensor sensor2 = new Sensor("testSensor2", SensorType.MOTION);
        sensor1.setActive(false);
        sensor2.setActive(true);
        Set<Sensor> sensors = new HashSet<>();
        sensors.add(sensor1);
        sensors.add(sensor2);
        when(securityRepository.getSensors()).thenReturn(sensors);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(false);
        securityService.processImage(image1);
        verify(securityRepository, times(0)).setAlarmStatus(AlarmStatus.NO_ALARM);
        sensor2.setActive(false);
        securityService.processImage(image1);
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }
    // If the system is armed-home while the camera shows a cat, set the alarm status to alarm.
    // 11
    @Test
    void testImageProcess03(){
        BufferedImage image1 = new BufferedImage(300, 400, BufferedImage.TYPE_INT_RGB);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);
        securityService.processImage(image1);
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }


}
