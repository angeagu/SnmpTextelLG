/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Pruebas;
import java.util.Date;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

/**
 *
 * @author Tecnovision
 */
public class SendingNotifications {

    public static final String community = "public";

    // Sending Trap for sysLocation of RFC1213
    public static final String Oid = ".1.3.6.1.4.1.46536.2.6.1";

    //IP of Local Host
    public static final String ipAddress = "192.168.1.7";

    //Ideally Port 162 should be used to send receive Trap, any other available Port can be used
    public static final int port = 162
            ;

    public static void main(String[] args) {
        SendingNotifications trapV2 = new SendingNotifications();
        trapV2.sendTrap_Version2();
    }

    /**
     * This methods sends the V1 trap to the Localhost in port 162
     */
    public void sendTrap_Version2() {
        try {
            // Create Transport Mapping
            TransportMapping transport = new DefaultUdpTransportMapping();
            transport.listen();

            // Create Target
            CommunityTarget cTarget = new CommunityTarget();
            cTarget.setCommunity(new OctetString(community));
            cTarget.setVersion(SnmpConstants.version2c);
            cTarget.setAddress(new UdpAddress(ipAddress + "/" + port));
            cTarget.setRetries(2);
            cTarget.setTimeout(5000);

            // Create PDU for V2
            PDU pdu = new PDU();

            // need to specify the system up time
            pdu.add(new VariableBinding(SnmpConstants.sysUpTime,
                    new OctetString(new Date().toString())));
            pdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, new OID(
                    Oid)));
            pdu.add(new VariableBinding(SnmpConstants.snmpTrapAddress,
                    new IpAddress(ipAddress)));

            pdu.add(new VariableBinding(new OID(Oid), new Integer32(150)));
            pdu.setType(PDU.NOTIFICATION);

            // Send the PDU
            Snmp snmp = new Snmp(transport);
            System.out.println("Sending V2 Trap... Check Wheather NMS is Listening or not? ");
            snmp.send(pdu, cTarget);
            snmp.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
