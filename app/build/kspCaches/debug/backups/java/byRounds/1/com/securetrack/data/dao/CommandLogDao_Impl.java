package com.securetrack.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.securetrack.data.Converters;
import com.securetrack.data.entities.CommandLog;
import com.securetrack.data.entities.CommandStatus;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class CommandLogDao_Impl implements CommandLogDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<CommandLog> __insertionAdapterOfCommandLog;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<CommandLog> __deletionAdapterOfCommandLog;

  private final EntityDeletionOrUpdateAdapter<CommandLog> __updateAdapterOfCommandLog;

  private final SharedSQLiteStatement __preparedStmtOfUpdateLogStatus;

  private final SharedSQLiteStatement __preparedStmtOfUpdateLogLocation;

  private final SharedSQLiteStatement __preparedStmtOfClearAllLogs;

  private final SharedSQLiteStatement __preparedStmtOfDeleteLogsOlderThan;

  public CommandLogDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCommandLog = new EntityInsertionAdapter<CommandLog>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `command_logs` (`id`,`command`,`senderNumber`,`timestamp`,`status`,`resultMessage`,`locationLat`,`locationLng`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CommandLog entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getCommand());
        statement.bindString(3, entity.getSenderNumber());
        statement.bindLong(4, entity.getTimestamp());
        final String _tmp = __converters.fromCommandStatus(entity.getStatus());
        statement.bindString(5, _tmp);
        if (entity.getResultMessage() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getResultMessage());
        }
        if (entity.getLocationLat() == null) {
          statement.bindNull(7);
        } else {
          statement.bindDouble(7, entity.getLocationLat());
        }
        if (entity.getLocationLng() == null) {
          statement.bindNull(8);
        } else {
          statement.bindDouble(8, entity.getLocationLng());
        }
      }
    };
    this.__deletionAdapterOfCommandLog = new EntityDeletionOrUpdateAdapter<CommandLog>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `command_logs` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CommandLog entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfCommandLog = new EntityDeletionOrUpdateAdapter<CommandLog>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `command_logs` SET `id` = ?,`command` = ?,`senderNumber` = ?,`timestamp` = ?,`status` = ?,`resultMessage` = ?,`locationLat` = ?,`locationLng` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CommandLog entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getCommand());
        statement.bindString(3, entity.getSenderNumber());
        statement.bindLong(4, entity.getTimestamp());
        final String _tmp = __converters.fromCommandStatus(entity.getStatus());
        statement.bindString(5, _tmp);
        if (entity.getResultMessage() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getResultMessage());
        }
        if (entity.getLocationLat() == null) {
          statement.bindNull(7);
        } else {
          statement.bindDouble(7, entity.getLocationLat());
        }
        if (entity.getLocationLng() == null) {
          statement.bindNull(8);
        } else {
          statement.bindDouble(8, entity.getLocationLng());
        }
        statement.bindLong(9, entity.getId());
      }
    };
    this.__preparedStmtOfUpdateLogStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE command_logs SET status = ?, resultMessage = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateLogLocation = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE command_logs SET locationLat = ?, locationLng = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearAllLogs = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM command_logs";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteLogsOlderThan = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM command_logs WHERE timestamp < ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertLog(final CommandLog log, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfCommandLog.insertAndReturnId(log);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteLog(final CommandLog log, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfCommandLog.handle(log);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateLog(final CommandLog log, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfCommandLog.handle(log);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateLogStatus(final long id, final CommandStatus status, final String message,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateLogStatus.acquire();
        int _argIndex = 1;
        final String _tmp = __converters.fromCommandStatus(status);
        _stmt.bindString(_argIndex, _tmp);
        _argIndex = 2;
        if (message == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, message);
        }
        _argIndex = 3;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateLogStatus.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateLogLocation(final long id, final double lat, final double lng,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateLogLocation.acquire();
        int _argIndex = 1;
        _stmt.bindDouble(_argIndex, lat);
        _argIndex = 2;
        _stmt.bindDouble(_argIndex, lng);
        _argIndex = 3;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateLogLocation.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearAllLogs(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAllLogs.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearAllLogs.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteLogsOlderThan(final long timestamp,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteLogsOlderThan.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, timestamp);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteLogsOlderThan.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<CommandLog>> getAllLogs() {
    final String _sql = "SELECT * FROM command_logs ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"command_logs"}, new Callable<List<CommandLog>>() {
      @Override
      @NonNull
      public List<CommandLog> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCommand = CursorUtil.getColumnIndexOrThrow(_cursor, "command");
          final int _cursorIndexOfSenderNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "senderNumber");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfResultMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "resultMessage");
          final int _cursorIndexOfLocationLat = CursorUtil.getColumnIndexOrThrow(_cursor, "locationLat");
          final int _cursorIndexOfLocationLng = CursorUtil.getColumnIndexOrThrow(_cursor, "locationLng");
          final List<CommandLog> _result = new ArrayList<CommandLog>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CommandLog _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpCommand;
            _tmpCommand = _cursor.getString(_cursorIndexOfCommand);
            final String _tmpSenderNumber;
            _tmpSenderNumber = _cursor.getString(_cursorIndexOfSenderNumber);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final CommandStatus _tmpStatus;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.toCommandStatus(_tmp);
            final String _tmpResultMessage;
            if (_cursor.isNull(_cursorIndexOfResultMessage)) {
              _tmpResultMessage = null;
            } else {
              _tmpResultMessage = _cursor.getString(_cursorIndexOfResultMessage);
            }
            final Double _tmpLocationLat;
            if (_cursor.isNull(_cursorIndexOfLocationLat)) {
              _tmpLocationLat = null;
            } else {
              _tmpLocationLat = _cursor.getDouble(_cursorIndexOfLocationLat);
            }
            final Double _tmpLocationLng;
            if (_cursor.isNull(_cursorIndexOfLocationLng)) {
              _tmpLocationLng = null;
            } else {
              _tmpLocationLng = _cursor.getDouble(_cursorIndexOfLocationLng);
            }
            _item = new CommandLog(_tmpId,_tmpCommand,_tmpSenderNumber,_tmpTimestamp,_tmpStatus,_tmpResultMessage,_tmpLocationLat,_tmpLocationLng);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<CommandLog>> getRecentLogs(final int limit) {
    final String _sql = "SELECT * FROM command_logs ORDER BY timestamp DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"command_logs"}, new Callable<List<CommandLog>>() {
      @Override
      @NonNull
      public List<CommandLog> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCommand = CursorUtil.getColumnIndexOrThrow(_cursor, "command");
          final int _cursorIndexOfSenderNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "senderNumber");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfResultMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "resultMessage");
          final int _cursorIndexOfLocationLat = CursorUtil.getColumnIndexOrThrow(_cursor, "locationLat");
          final int _cursorIndexOfLocationLng = CursorUtil.getColumnIndexOrThrow(_cursor, "locationLng");
          final List<CommandLog> _result = new ArrayList<CommandLog>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CommandLog _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpCommand;
            _tmpCommand = _cursor.getString(_cursorIndexOfCommand);
            final String _tmpSenderNumber;
            _tmpSenderNumber = _cursor.getString(_cursorIndexOfSenderNumber);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final CommandStatus _tmpStatus;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.toCommandStatus(_tmp);
            final String _tmpResultMessage;
            if (_cursor.isNull(_cursorIndexOfResultMessage)) {
              _tmpResultMessage = null;
            } else {
              _tmpResultMessage = _cursor.getString(_cursorIndexOfResultMessage);
            }
            final Double _tmpLocationLat;
            if (_cursor.isNull(_cursorIndexOfLocationLat)) {
              _tmpLocationLat = null;
            } else {
              _tmpLocationLat = _cursor.getDouble(_cursorIndexOfLocationLat);
            }
            final Double _tmpLocationLng;
            if (_cursor.isNull(_cursorIndexOfLocationLng)) {
              _tmpLocationLng = null;
            } else {
              _tmpLocationLng = _cursor.getDouble(_cursorIndexOfLocationLng);
            }
            _item = new CommandLog(_tmpId,_tmpCommand,_tmpSenderNumber,_tmpTimestamp,_tmpStatus,_tmpResultMessage,_tmpLocationLat,_tmpLocationLng);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getLogById(final long id, final Continuation<? super CommandLog> $completion) {
    final String _sql = "SELECT * FROM command_logs WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<CommandLog>() {
      @Override
      @Nullable
      public CommandLog call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCommand = CursorUtil.getColumnIndexOrThrow(_cursor, "command");
          final int _cursorIndexOfSenderNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "senderNumber");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfResultMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "resultMessage");
          final int _cursorIndexOfLocationLat = CursorUtil.getColumnIndexOrThrow(_cursor, "locationLat");
          final int _cursorIndexOfLocationLng = CursorUtil.getColumnIndexOrThrow(_cursor, "locationLng");
          final CommandLog _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpCommand;
            _tmpCommand = _cursor.getString(_cursorIndexOfCommand);
            final String _tmpSenderNumber;
            _tmpSenderNumber = _cursor.getString(_cursorIndexOfSenderNumber);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final CommandStatus _tmpStatus;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.toCommandStatus(_tmp);
            final String _tmpResultMessage;
            if (_cursor.isNull(_cursorIndexOfResultMessage)) {
              _tmpResultMessage = null;
            } else {
              _tmpResultMessage = _cursor.getString(_cursorIndexOfResultMessage);
            }
            final Double _tmpLocationLat;
            if (_cursor.isNull(_cursorIndexOfLocationLat)) {
              _tmpLocationLat = null;
            } else {
              _tmpLocationLat = _cursor.getDouble(_cursorIndexOfLocationLat);
            }
            final Double _tmpLocationLng;
            if (_cursor.isNull(_cursorIndexOfLocationLng)) {
              _tmpLocationLng = null;
            } else {
              _tmpLocationLng = _cursor.getDouble(_cursorIndexOfLocationLng);
            }
            _result = new CommandLog(_tmpId,_tmpCommand,_tmpSenderNumber,_tmpTimestamp,_tmpStatus,_tmpResultMessage,_tmpLocationLat,_tmpLocationLng);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getCountByStatus(final CommandStatus status,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM command_logs WHERE status = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    final String _tmp = __converters.fromCommandStatus(status);
    _statement.bindString(_argIndex, _tmp);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(0);
            _result = _tmp_1;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
