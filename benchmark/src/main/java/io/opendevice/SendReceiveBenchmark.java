/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.opendevice;

import br.com.criativasoft.opendevice.connection.UsbConnection;
import br.com.criativasoft.opendevice.core.LocalDeviceManager;
import br.com.criativasoft.opendevice.core.command.DeviceCommand;
import br.com.criativasoft.opendevice.core.command.ResponseCommand;
import br.com.criativasoft.opendevice.core.connection.Connections;
import br.com.criativasoft.opendevice.core.model.Device;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class SendReceiveBenchmark {

    private Device device = new Device(1, Device.DIGITAL);

    private LocalDeviceManager manager;
    private AtomicBoolean toggle = new AtomicBoolean(false);

    @Setup(Level.Trial)
    public void setup() {
        UsbConnection.BAUDRATE = 115200;

        manager = new LocalDeviceManager();
//        manager.addDevice(device);


        try {
//        manager.connect(Connections.out.tcp("192.168.1.112:" + TCPConnection.DEFAULT_PORT));
        manager.connect(Connections.out.usb());
//            manager.connect(Connections.out.bluetooth("00:11:06:14:04:57"));
            System.out.println("Connected !");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @TearDown(Level.Trial)
    public void showdown() {
            manager.stop();
    }


//    @State(Scope.Benchmark)
//    public static class DeviceCommandToSend {
//        final DeviceCommand command = DeviceCommand.ON(1);
//    }


    @Benchmark
    public ResponseCommand sendAndWait() throws IOException {
        DeviceCommand command;
        if (toggle.get()) {
            command = DeviceCommand.ON(1);
            toggle.set(false);
        } else {
            command = DeviceCommand.OFF(1);
            toggle.set(true);
        }
        manager.send(command);

        ResponseCommand response = command.getResponse();
        if(response == null){
            System.out.println("No response...");
        }
        return response;
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(SendReceiveBenchmark.class.getSimpleName())
                .mode(Mode.AverageTime)
                .resultFormat(ResultFormatType.CSV)
                .result("benchmark.csv")
                // Warm-up setup.
                .warmupIterations(2)
                .warmupTime(TimeValue.milliseconds(2000))
                // Measurement setup.
                .measurementIterations(20)
                .measurementTime(TimeValue.milliseconds(1000))
                // Fork! (Invoke benchmarks in separate JVM)
                .forks(1)
                .timeout(TimeValue.seconds(2))
                // .jvmArgs("-client", "-XX:+AggressiveOpts", "-XX:-TieredCompilation")
                .jvmArgs("-server")
//                .addProfiler(StackProfiler.class)
                .build();


        new Runner(options).run();
    }

}
