package skynetbee.developers.developerenvironment

import skynetbee.developers.production.DF


fun notify(uid: String, msg: String) {
    val insertQuery = """
        INSERT INTO send_msg_via_unique_member_id (
            uid, message, area, mcounti, fromdat, ftodat, ftotim,
            ftovername, ftover, ftopid, todat, totim, tovername, tover, topid,
            ipmac, deviceanduserainfo, basesite, owncomcode, testeridentity,
            testcontrol, adderpid, addername, adder, syncstatus, doe, toe
        ) VALUES (
            '$uid', '$msg', 'Chennai', 'MC001', '', '', '10:30:00',
            'OverName1', 'Over1', 'PID001', '2025-03-26', '15:45:00', 'OverName2', 'Over2', 'PID002',
            '192.168.1.1', 'MacOS-Safari', 'BaseSite1', 'COM001', 'Tester1',
            'Control1', 'PID003', 'Adder1', 'AdderDetail', 'SYNCED', '${getCurrentDate()}', '${getCurrentTime()}'
        );
    """.trimIndent()

    DF.executeQuery(insertQuery)
}



