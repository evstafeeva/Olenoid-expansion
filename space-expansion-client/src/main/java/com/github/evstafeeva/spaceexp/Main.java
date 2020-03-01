package com.github.evstafeeva.spaceexp;

import com.github.evstafeeva.spaceexp.Geometry.Point;
import com.github.evstafeeva.spaceexp.Geometry.Position;
import com.github.evstafeeva.spaceexp.Geometry.Vector;
import com.github.evstafeeva.spaceexp.modules.*;
import com.github.evstafeeva.spaceexp.tacticalcore.MoveProcedure;
import com.github.evstafeeva.spaceexp.transport.ProtobufChannel;
import com.github.evstafeeva.spaceexp.transport.UdpChannel;
import com.github.evstafeeva.spaceexp.transport.VirtualChannel;

public class Main {
    public static void main(String[] args) {
        String localIP = args[0];
        int localPort = Integer.parseInt(args[1]);
        String remoteIP = args[2];
        int remotePort = Integer.parseInt(args[3]);

        System.out.println("Local: " + localIP + ":" + localPort);
        System.out.println("Remote: " + remoteIP + ":" + remotePort);

        // Создаём компоненты, реализующие транспорт в сторону сервера:
        UdpChannel udpChannel = new UdpChannel(localIP, localPort);
        udpChannel.setRemoteAddress(remoteIP, remotePort);

        ProtobufChannel protobufChannel = new ProtobufChannel();
        protobufChannel.linkToChannel(udpChannel);

        // Создаём компоненту AccessPanel, с помощью которой будем авторизовываться на сервере
        // и подключаем её к Protobuf-каналу
        AccessPanel accessPanel = new AccessPanel();
        accessPanel.linkToChannel(protobufChannel);

        // Подключаем канал к AccessPanel'у и запускаем работу protobuf-канала в отдельном потоке
        // Канал будет постоянно читать сокет и прокидывать наверх (в данном случае в accessPanel) все получаемые
        // сообщения через функцию ITerminal.onMessageReceived()
        protobufChannel.linkToTerminal(accessPanel);
        Thread transportThread = new Thread() {
            public void run() {
                protobufChannel.run();
            }
        };
        transportThread.start();

        // Теперь транспорт запущен и работает, можем запустить процедуру логина на сервере
        AccessPanel.LoginStatus status = accessPanel.login("Olenoid", "admin", localIP, localPort);

        if (!status.success) {
            System.out.println("Loshara: " + status.problem);
            return;
        }
        System.out.println("Success: use server's port " + status.port);

        // После того, как сервер сообщил об успешной авторизации, компонента AccessPanel больше не нужна.
        // Теперь перенастраиваем UdpChannel на новый порт
        udpChannel.setRemoteAddress(remoteIP, status.port);

        // Создаём корневой коммутатор и связываем его с protobuf-каналом
        Commutator rootCommutator = new Commutator();
        rootCommutator.linkToChannel(protobufChannel);
        protobufChannel.linkToTerminal(rootCommutator);

        exploreCommutator(rootCommutator, "Root");

        VirtualChannel channelToShip = rootCommutator.openTunnel(1);

        Ship ship = new Ship();
        ship.linkToChannel(channelToShip);
        channelToShip.linkToTerminal(ship);

        Engine engine = new Engine();
        {
            VirtualChannel channel = ship.openTunnelTo("engine", "Engine");
            engine.linkToChannel(channel);
            channel.linkToTerminal(engine);
        }

        MoveProcedure moving = new MoveProcedure(ship, engine, new Point(2000, 5000));
        moving.run();
        System.out.println("Current position: " + ship.getPosition());
        while (true) {
            try {
                Thread.sleep(50);
            } catch (Exception exception) {
            }
            System.out.println("Current position: " + ship.getPosition());
        }
    }

    private static void exploreCommutator(Commutator commutator, String name) {
        System.out.println("Exploring commutator '" + name + "'...");
        int nTotalSlots = commutator.getTotalSlots();
        System.out.println("  Total slots: " + nTotalSlots);

        for (int slotId = 0; slotId < nTotalSlots; ++slotId) {
            ModuleInfo info = commutator.getModuleInfo(slotId);
            System.out.println("  Slot #" + slotId + ": " + info.getModuleType() + " \"" + info.getModuleName() + "\"");
        }
    }

}
