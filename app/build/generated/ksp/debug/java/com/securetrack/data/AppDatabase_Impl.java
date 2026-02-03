package com.securetrack.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.securetrack.data.dao.CommandLogDao;
import com.securetrack.data.dao.CommandLogDao_Impl;
import com.securetrack.data.dao.EmergencyContactDao;
import com.securetrack.data.dao.EmergencyContactDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile EmergencyContactDao _emergencyContactDao;

  private volatile CommandLogDao _commandLogDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `emergency_contacts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `phoneNumber` TEXT NOT NULL, `isPrimary` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `command_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `command` TEXT NOT NULL, `senderNumber` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `status` TEXT NOT NULL, `resultMessage` TEXT, `locationLat` REAL, `locationLng` REAL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '72b10d5ab4d157dc11347811b529af95')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `emergency_contacts`");
        db.execSQL("DROP TABLE IF EXISTS `command_logs`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsEmergencyContacts = new HashMap<String, TableInfo.Column>(6);
        _columnsEmergencyContacts.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEmergencyContacts.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEmergencyContacts.put("phoneNumber", new TableInfo.Column("phoneNumber", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEmergencyContacts.put("isPrimary", new TableInfo.Column("isPrimary", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEmergencyContacts.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEmergencyContacts.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysEmergencyContacts = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesEmergencyContacts = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoEmergencyContacts = new TableInfo("emergency_contacts", _columnsEmergencyContacts, _foreignKeysEmergencyContacts, _indicesEmergencyContacts);
        final TableInfo _existingEmergencyContacts = TableInfo.read(db, "emergency_contacts");
        if (!_infoEmergencyContacts.equals(_existingEmergencyContacts)) {
          return new RoomOpenHelper.ValidationResult(false, "emergency_contacts(com.securetrack.data.entities.EmergencyContact).\n"
                  + " Expected:\n" + _infoEmergencyContacts + "\n"
                  + " Found:\n" + _existingEmergencyContacts);
        }
        final HashMap<String, TableInfo.Column> _columnsCommandLogs = new HashMap<String, TableInfo.Column>(8);
        _columnsCommandLogs.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCommandLogs.put("command", new TableInfo.Column("command", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCommandLogs.put("senderNumber", new TableInfo.Column("senderNumber", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCommandLogs.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCommandLogs.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCommandLogs.put("resultMessage", new TableInfo.Column("resultMessage", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCommandLogs.put("locationLat", new TableInfo.Column("locationLat", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCommandLogs.put("locationLng", new TableInfo.Column("locationLng", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCommandLogs = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCommandLogs = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCommandLogs = new TableInfo("command_logs", _columnsCommandLogs, _foreignKeysCommandLogs, _indicesCommandLogs);
        final TableInfo _existingCommandLogs = TableInfo.read(db, "command_logs");
        if (!_infoCommandLogs.equals(_existingCommandLogs)) {
          return new RoomOpenHelper.ValidationResult(false, "command_logs(com.securetrack.data.entities.CommandLog).\n"
                  + " Expected:\n" + _infoCommandLogs + "\n"
                  + " Found:\n" + _existingCommandLogs);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "72b10d5ab4d157dc11347811b529af95", "504690285373fa4f2987ffe96739eaf2");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "emergency_contacts","command_logs");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `emergency_contacts`");
      _db.execSQL("DELETE FROM `command_logs`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(EmergencyContactDao.class, EmergencyContactDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(CommandLogDao.class, CommandLogDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public EmergencyContactDao emergencyContactDao() {
    if (_emergencyContactDao != null) {
      return _emergencyContactDao;
    } else {
      synchronized(this) {
        if(_emergencyContactDao == null) {
          _emergencyContactDao = new EmergencyContactDao_Impl(this);
        }
        return _emergencyContactDao;
      }
    }
  }

  @Override
  public CommandLogDao commandLogDao() {
    if (_commandLogDao != null) {
      return _commandLogDao;
    } else {
      synchronized(this) {
        if(_commandLogDao == null) {
          _commandLogDao = new CommandLogDao_Impl(this);
        }
        return _commandLogDao;
      }
    }
  }
}
