package org.platformlayer.ops;

import java.io.File;

import org.platformlayer.crypto.Md5Hash;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.ops.process.ProcessExecution;
import org.platformlayer.ops.process.ProcessExecutionException;

/**
 * Creates a target out of machine image that we have to chroot into. Although the default implementations of many
 * OpsTarget might not require overloading many methods, we do overload them so we don't make assumptions about the
 * implementation. e.g. readTextFile likely calls execute('cat {0}'), which would then get chrooted; but if it switched
 * to use scp then this would break.
 * 
 */
public class ChrootOpsTarget extends OpsTargetBase {
	final File chrootDir;
	final File tmpDir;
	final OpsTarget parentTarget;

	public ChrootOpsTarget(File chrootDir, File tmpDir, OpsTarget parentTarget) {
		this.chrootDir = chrootDir;
		this.tmpDir = tmpDir;
		this.parentTarget = parentTarget;
	}

	@Override
	public File createTempDir() throws OpsException {
		// TODO: Auto delete tempdir?
		return createTempDir(tmpDir);
	}

	static File mapToOutsideChroot(File chrootDir, File file) {
		String path = file.getAbsolutePath();
		if (!path.startsWith("/")) {
			throw new IllegalStateException();
		}
		path = path.substring(1);
		return new File(chrootDir, path);
	}

	File mapToOutsideChroot(File file) {
		return mapToOutsideChroot(chrootDir, file);
	}

	@Override
	public void setFileContents(File path, byte[] contents) throws ProcessExecutionException {
		File innerFile = mapToOutsideChroot(path);
		parentTarget.setFileContents(innerFile, contents);
	}

	@Override
	public Md5Hash getFileHash(File path) throws OpsException {
		File innerFile = mapToOutsideChroot(path);
		return parentTarget.getFileHash(innerFile);
	}

	@Override
	protected ProcessExecution executeCommandUnchecked(Command command) throws ProcessExecutionException {
		// Command innerCommand = Command.build("chroot {0} {1}", chrootDir, command.buildCommandString());

		Command innerCommand = command.prefix("chroot", chrootDir);

		return parentTarget.executeCommand(innerCommand);
	}

	// @Override
	// public File createTempDir() throws OpsException {
	// return inner.createTempDir();
	// }

	@Override
	public void touchFile(File file) throws ProcessExecutionException {
		parentTarget.touchFile(mapToOutsideChroot(file));
	}

	@Override
	public void mkdir(File dir) throws OpsException {
		parentTarget.mkdir(mapToOutsideChroot(dir));
	}

	@Override
	public String readTextFile(File file) throws OpsException {
		return parentTarget.readTextFile(file);
	}

	@Override
	public void chmod(File file, String mode) throws OpsException {
		parentTarget.chmod(mapToOutsideChroot(file), mode);
	}

	@Override
	public void rm(File file) throws OpsException {
		parentTarget.rm(mapToOutsideChroot(file));
	}

	@Override
	public boolean isSameMachine(OpsTarget target) {
		log.warn("isSameMachine stub-implements for ChrootOpsTarget");
		return false;
	}

	@Override
	public NetworkPoint getNetworkPoint() {
		throw new UnsupportedOperationException();
	}
}
