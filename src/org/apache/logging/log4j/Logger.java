package org.apache.logging.log4j;

public abstract interface Logger {

	public abstract void debug(Object paramObject);

	public abstract void debug(Object paramObject, Throwable paramThrowable);

	public abstract void debug(String paramString);

	public abstract void debug(String paramString, Object... paramVarArgs);

	public abstract void debug(String paramString, Object paramObject);

	public abstract void debug(String paramString, Object paramObject1, Object paramObject2);

	public abstract void debug(String paramString, Object paramObject1, Object paramObject2, Object paramObject3);

	public abstract void error(String paramString);

	public abstract void error(String paramString, Object... paramVarArgs);

	public abstract void error(String paramString, Throwable paramThrowable);

	public abstract void error(String paramString, Object paramObject);

	public abstract void error(String paramString, Object paramObject1, Object paramObject2);

	public abstract void error(String paramString, Object paramObject1, Object paramObject2, Object paramObject3);

	public abstract void fatal(String paramString);

	public abstract void fatal(String paramString, Object... paramVarArgs);

	public abstract void fatal(String paramString, Throwable paramThrowable);

	public abstract void fatal(String paramString, Object paramObject);

	public abstract void fatal(String paramString, Object paramObject1, Object paramObject2);

	public abstract void fatal(String paramString, Object paramObject1, Object paramObject2, Object paramObject3);

	public abstract void info(Object paramObject);

	public abstract void info(Object paramObject, Throwable paramThrowable);

	public abstract void info(String paramString);

	public abstract void info(String paramString, Object... paramVarArgs);

	public abstract void info(String paramString, Throwable paramThrowable);

	public abstract void info(String paramString, Object paramObject);

	public abstract void info(String paramString, Object paramObject1, Object paramObject2);

	public abstract void info(String paramString, Object paramObject1, Object paramObject2, Object paramObject3);

	public abstract void warn(Object paramObject);

	public abstract void warn(Object paramObject, Throwable paramThrowable);

	public abstract void warn(String paramString);

	public abstract void warn(String paramString, Object... paramVarArgs);

	public abstract void warn(String paramString, Throwable paramThrowable);

	public abstract void warn(String paramString, Object paramObject);

	public abstract void warn(String paramString, Object paramObject1, Object paramObject2);

	public abstract void warn(String paramString, Object paramObject1, Object paramObject2, Object paramObject3);
}
