package datawake.datadriven.databasesync.backoffice.seeds;

import datawake.datadriven.databasesync.shared.enums.DriverClassNameEnum;
import datawake.datadriven.databasesync.core.models.Connection;
import datawake.datadriven.databasesync.core.models.ConnectionTable;
import datawake.datadriven.databasesync.core.models.Table;
import datawake.datadriven.databasesync.core.models.keys.ConnectionTableKey;
import datawake.datadriven.databasesync.core.services.ConnectionsService;
import datawake.datadriven.databasesync.core.services.TablesService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@Lazy(value = false)
@Profile("engrecon-seed")
public class EngreconSeed {
    private static final Logger log = LoggerFactory.getLogger(EngreconSeed.class);

    private final ConnectionsService connectionsService;

    private final TablesService tablesService;

    @Qualifier("transactionManager")
    private final PlatformTransactionManager transactionManager;

    public EngreconSeed(ConnectionsService connectionsService, TablesService tablesService, PlatformTransactionManager transactionManager) {
        this.connectionsService = connectionsService;
        this.tablesService = tablesService;
        this.transactionManager = transactionManager;
    }

    @PostConstruct
    public void initDatabase() {
        log.info("SEED RUNNING");
        final String name = "ENGRECON-ISOQUALITAS";

        if (connectionsService.existsByName(name))
            return;

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {

                Connection connection = new Connection();
                connection.setDriverClassname(DriverClassNameEnum.SQLServer);
                connection.setName(name);
                connection.setUrl("jdbc:sqlserver://;serverName=177.126.4.174;port=9991;encrypt=false");
                connection.setUsername("ISOQUALITAS");
                connection.setPassword("ISOQ");

                connection = connectionsService.save(connection);

                Table table1 = new Table();
                table1.setLabel("Instrução de PSS");
                table1.setName("InstrPss");
                table1.setPrimaryKeys("PcNum,PcRev,OpeId,InstrIdx,PssNum");

                Table table2 = new Table();
                table2.setLabel("Instrução de Setup");
                table2.setName("InstrSetup");
                table2.setPrimaryKeys("PcNum,PcRev,OpeId,InstrIdx,SetupId,SetupTp");

                Table table3 = new Table();
                table3.setLabel("Registro de Inspeção");
                table3.setName("reg_inspecao");
                table3.setPrimaryKeys("PcNum,PcRev,OpeId,CarId,InstrIdx");

                table1 = tablesService.save(table1);
                table2 = tablesService.save(table2);
                table3 = tablesService.save(table3);

                ConnectionTable connectionTable1 = new ConnectionTable();
                connectionTable1.setId(ConnectionTableKey.generate(connection.getId(), table1.getId()));
                connectionTable1.setConnection(connection);
                connectionTable1.setTable(table1);

                ConnectionTable connectionTable2 = new ConnectionTable();
                connectionTable2.setId(ConnectionTableKey.generate(connection.getId(), table2.getId()));
                connectionTable2.setConnection(connection);
                connectionTable2.setTable(table2);

                ConnectionTable connectionTable3 = new ConnectionTable();
                connectionTable3.setId(ConnectionTableKey.generate(connection.getId(), table3.getId()));
                connectionTable3.setConnection(connection);
                connectionTable3.setTable(table3);
                connectionTable3.setCustomQuery(customQuery);

                connection.getConnectionsTables().add(connectionTable1);
                connection.getConnectionsTables().add(connectionTable2);
                connection.getConnectionsTables().add(connectionTable3);

                connectionsService.save(connection);
            }
        });
    }

    private final String customQuery = """
            SELECT\s
                Pc.PcNum, Pc.PcRev, Pc.PcNumCli, Pc.PcNom, Pc.PcCli, Pc.PcDtRev, Pc.PcNumDwgCliRev,\s
                Ope.OpeId, Ope.OpeNum, Ope.OpeDscr, Ope.OpeMaq, Ope.OpeTp, \s
                Car.CarId, Car.CarNum, Car.CarDscr, Car.CarSpec, Car.CarLie, Car.CarLse, Car.CarTol, Car.CarNomin, Car.CarTolPlus, Car.CarTolMinus, Car.CarEsp,\s
                Instr.InstrIdx,
                CarOpeInstr.CarOpeMeio, CarOpeInstr.CarOpeMetCtrl, CarOpeInstr.CarOpeTamAmst, CarOpeInstr.CarOpeFreqAmst, CarOpeInstr.CarOpePlnReac, CarOpeInstr.CtrlTp, CarOpeInstr.CtrlPor\s

            FROM Pc\s
            LEFT OUTER JOIN CarOpeInstr\s
                ON CarOpeInstr.PcNum = Pc.PcNum\s
                AND CarOpeInstr.PcRev = Pc.PcRev\s
            JOIN Ope\s
                ON Ope.PcNum = Pc.PcNum\s
                AND Ope.PcRev = Pc.PcRev\s
                AND Ope.OpeId = CarOpeInstr.OpeId
            LEFT OUTER JOIN Car\s
                ON Car.PcNum = Pc.PcNum\s
                AND Car.PcRev = Pc.PcRev\s
                AND Car.CarId = CarOpeInstr.CarId
            LEFT OUTER JOIN Instr\s
                ON Instr.PcNum = Pc.PcNum\s
                AND Instr.PcRev = Pc.PcRev\s
                AND Instr.OpeId = Ope.OpeId\s
            WHERE Pc.PcRev = (SELECT MAX(PcRev) PcRev\s
                FROM Pc AS maxRev\s
                WHERE maxRev.PcNum = Pc.PcNum);
            """;
}

//CRIACAO DA TABELA REG INSPECAO
//CREATE TABLE dw_engrecon.dbo.reg_inspecao (
//        id INT IDENTITY(1,1),
//        PcNumDwgCliRev nvarchar(20) COLLATE Latin1_General_CI_AS NULL,
//        OpeTp nvarchar(1) COLLATE Latin1_General_CI_AS NULL,
//        CtrlTp nvarchar(1) COLLATE Latin1_General_CI_AS NULL,
//        CtrlPor nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
//        CarOpePlnReac nvarchar(MAX) COLLATE Latin1_General_CI_AS NULL,
//        CarOpeFreqAmst nvarchar(100) COLLATE Latin1_General_CI_AS NULL,
//        CarOpeTamAmst nvarchar(100) COLLATE Latin1_General_CI_AS NULL,
//        CarOpeMetCtrl nvarchar(MAX) COLLATE Latin1_General_CI_AS NULL,
//        CarOpeMeio nvarchar(MAX) COLLATE Latin1_General_CI_AS NULL,
//        InstrIdx nvarchar(50) COLLATE Latin1_General_CI_AS NOT NULL,
//        CarEsp bit NULL,
//        CarTolMinus float NULL,
//        CarTolPlus float NULL,
//        CarNomin float NULL,
//        CarTol float NULL,
//        CarLse float NULL,
//        CarLie float NULL,
//        CarSpec varchar(200) COLLATE Latin1_General_CI_AS NULL,
//        CarDscr varchar(200) COLLATE Latin1_General_CI_AS NULL,
//        CarNum nvarchar(10) COLLATE Latin1_General_CI_AS NULL,
//        CarId nvarchar(5) COLLATE Latin1_General_CI_AS NOT NULL,
//        OpeMaq varchar(255) COLLATE Latin1_General_CI_AS NULL,
//        OpeDscr varchar(255) COLLATE Latin1_General_CI_AS NULL,
//        OpeId nvarchar(5) COLLATE Latin1_General_CI_AS NOT NULL,
//        OpeNum nvarchar(20) COLLATE Latin1_General_CI_AS NULL,
//        PcCli nvarchar(30) COLLATE Latin1_General_CI_AS NULL,
//        PcDtRev datetime NULL,
//        PcNom nvarchar(150) COLLATE Latin1_General_CI_AS NULL,
//        PcNumCli nvarchar(50) COLLATE Latin1_General_CI_AS NULL,
//        PcNum nvarchar(40) COLLATE Latin1_General_CI_AS NOT NULL,
//        PcRev nvarchar(20) COLLATE Latin1_General_CI_AS NOT NULL,
//        CONSTRAINT pk_reg_inspecao PRIMARY KEY (PcNum,PcRev,OpeId,CarId,InstrIdx)
//        );