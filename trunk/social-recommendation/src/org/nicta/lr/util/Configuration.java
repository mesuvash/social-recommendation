package org.nicta.lr.util;

public class Configuration 
{
	//Feature counts
	public static final int LINK_FEATURE_COUNT = 3;
	public static final int USER_FEATURE_COUNT = 3;
	
	//Windows
	public static final int TRAINING_WINDOW_RANGE = 30;
	public static final int RECOMMENDING_WINDOW_RANGE = 14;
	
	public static String DEPLOYMENT_TYPE = Constants.TEST;
	public static boolean INITIALIZE = true;
	
	public static String DB_STRING = Constants.SCOTT_DB_STRING;
	public static String LANG_PROFILE_FOLDER = Constants.LOCAL_LANG_PROFILE_FOLDER;
	
	public static String TRAINING_DATA = Constants.ACTIVE;
	public static String TEST_DATA = Constants.APP_USER_ACTIVE_ALL;
	
	//public static String TRAINING_DATA = Constants.PASSIVE;
	//public static String TEST_DATA = Constants.FB_USER_PASSIVE;
}
