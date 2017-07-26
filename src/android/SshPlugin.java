package com.calix.plugins;

import java.io.InputStream;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
public class SshPlugin extends CordovaPlugin {

	//TODO: Single instance only
	private static JSch jsch = new JSch();
	private static Session session = null;
	private static ChannelExec channel = null;
	//private static  channelExec = null;
	
	private static final String ACTION_CONNECT="connect";
	private static final String ACTION_DISCONNECT="disconnect";
	private static final String ACTION_DISCONNECTALL="disconnectAll";
	private static final String ACTION_AUTHENTICATEBYPASSWORD="authenticateByPassword";
	private static final String ACTION_AUTHENTICATEBYKEYBOARD="authenticateByKeyboard";
	private static final String ACTION_RUNCOMMAND="runCommand";
	
	private static final String tag = "SshPlugin";
	@Override
	public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
		if(ACTION_CONNECT.equals(action)){
			if(args.length() != 3){
				Log.e(tag, "Parameters error");
				callbackContext.error("Parameters error for:"+action);
				return false;
			} else {
				if (session != null){
					callbackContext.error("connection in use");
					Log.e(tag, "connection not closed before!");
					return false;
				} else {
					connect(args, callbackContext);
				}
			}
			return true;
		} else if(action.equals(ACTION_DISCONNECT)){
			disconnect(args, callbackContext);
			return true;
		} else if(this.ACTION_RUNCOMMAND.equals(action)){
			runCommand(args, callbackContext);
			return true;
		} else if(ACTION_AUTHENTICATEBYPASSWORD.equalsIgnoreCase(action)){
			authenticateByPassword(args, callbackContext);
			//callbackContext.success("Function " + action + " is called");
			return true;
		} else if(ACTION_AUTHENTICATEBYKEYBOARD.equals(action)){
			callbackContext.success("Function " + action + " is called! But it not supported now.");
			return true;
		} else if(this.ACTION_DISCONNECTALL.equals(action)){
			callbackContext.success("Function " + action + " is called! But it not supported now.");
			return true;			
		} else {
			Log.w(tag, "calling "+action);
			callbackContext.error("Error "+ action + " is not a supported function!");
			return false;
		}
	}

	private void runCommand(final JSONArray args,
			final CallbackContext callbackContext) throws JSONException {
		// new Thread() {
		cordova.getThreadPool().execute(new Runnable() {
			public void run() {
				try {
					if (session == null || !session.isConnected()) {
						Log.e(tag, "session not openned before");
						callbackContext.error("connection is not openned");
					} else {
						channel = (ChannelExec) session.openChannel("exec");
						if (channel != null) {
							Log.d(tag, "runCommand:" + args.getString(1));
							channel.setCommand(args.getString(1));
							channel.setInputStream(null);
							InputStream in = channel.getInputStream();
							channel.connect();
							StringBuffer data = new StringBuffer();
							byte[] tmp = new byte[1024];
							while (true) {
								while (in.available() > 0) {
									int i = in.read(tmp, 0, 1024);
									if (i < 0)
										break;
									data.append(new String(tmp, 0, i));
									System.out.print(new String(tmp, 0, i));
								}
								if (channel.isClosed()) {
									if (in.available() > 0)
										continue;
									System.out.println("exit-status: "
											+ channel.getExitStatus());
									break;
								}
								try {
									Thread.sleep(1000);
								} catch (Exception ee) {
								}
							}
							if (data.length() > 0) {
								Log.d(tag, data.toString());
								callbackContext.success(data.toString());
							} else {
								callbackContext.success("");
							}
						} else {
							callbackContext.error("exec channel cannot be openned");
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(tag, "Problem running command");
					try {
						callbackContext.error("Problem connecting "
								+ args.getString(0));
					} catch (JSONException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				} finally {
					if (channel != null) {
						channel.disconnect();
						channel = null;
						Log.d(tag, "close Channel");
					}
				}
			}
		});
		// }.start();
	}

	private void authenticateByPassword(final JSONArray args,
			final CallbackContext callbackContext) throws JSONException {
		cordova.getThreadPool().execute(new Runnable() {
			// new Thread() {
			public void run() {
				try {
					if (session != null) {
						if (session.isConnected()) {
							callbackContext.success("already authenticated");
						} else {
							session.setPassword(args.getString(1));
							session.setConfig("StrictHostKeyChecking", "no");
							session.connect();
							Log.d(tag,
									"authenticateByPassword:"
											+ args.getString(1));
							if (session.isConnected()) {
								callbackContext.success("authenticated OK");
							}
						}
					} else {
						callbackContext.error("connection not openned");
					}
				} catch (Exception e) {
					e.printStackTrace();
					try {
						Log.e(tag,
								"Problem authenticating host with:"
										+ args.getString(1));
					} catch (JSONException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					try {
						callbackContext
								.error("Problem authenticating host with:"
										+ args.getString(1));
					} catch (JSONException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						callbackContext
								.error("Error authentication with parameter error");
					}
				}
			}
		});// .start();
	}

	private void connect(final JSONArray args,
			final CallbackContext callbackContext) throws JSONException {
		// new Thread() {
		cordova.getThreadPool().execute(new Runnable() {
			public void run() {
				try {
					session = jsch.getSession(args.getString(2),
							args.getString(0), args.getInt(1));
					if(session != null){
						callbackContext.success("connection OK");
					} else {
						callbackContext.error("Error connect");
					}
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(tag, "Problem connecting host");
					try {
						callbackContext.error("Problem connecting "
								+ args.getString(0));
					} catch (JSONException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});// .start();
	}

	private void disconnect(final JSONArray args,
			final CallbackContext callbackContext) throws JSONException {
		cordova.getThreadPool().execute(new Runnable() {
			// new Thread() {
			public void run() {
				try {
					if (channel != null) {
						channel.disconnect();
						channel = null;
					} else {
						Log.e(tag, "channel closed before");
					}
					if (session != null) {
						session.disconnect();
						session = null;
					} else {
						Log.e(tag, "session closed before");
					}
					callbackContext.success("connection disconnected");
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(tag, "Problem disconnecting host");
					try {
						callbackContext.error("Problem disconnecting "
								+ args.getString(0));
					} catch (JSONException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});// .start();
	}
	private void tryConnect(){
		try {
			JSch jsch = new JSch();

			Session session = jsch.getSession("root", "10.1.64.50", 22);
			session.setPassword("root");
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect();

			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand("pwd");

			// X Forwarding
			// channel.setXForwarding(true);

			// channel.setInputStream(System.in);
			channel.setInputStream(null);

			// channel.setOutputStream(System.out);

			// FileOutputStream fos=new FileOutputStream("/tmp/stderr");
			// ((ChannelExec)channel).setErrStream(fos);
			((ChannelExec) channel).setErrStream(System.err);

			InputStream in = channel.getInputStream();

			channel.connect();

			byte[] tmp = new byte[1024];
			while (true) {
				while (in.available() > 0) {
					int i = in.read(tmp, 0, 1024);
					if (i < 0)
						break;
					System.out.print(new String(tmp, 0, i));
				}
				if (channel.isClosed()) {
					if (in.available() > 0)
						continue;
					System.out.println("exit-status: "
							+ channel.getExitStatus());
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (Exception ee) {
				}
			}
			channel.disconnect();
			session.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e);
		}
	}
}


