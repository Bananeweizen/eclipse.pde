/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.api.tools.internal.builder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.pde.api.tools.internal.comparator.Delta;
import org.eclipse.pde.api.tools.internal.provisional.ApiPlugin;
import org.eclipse.pde.api.tools.internal.provisional.comparator.IDelta;

public class BuildState {
	private IDelta[] EMPTY_DELTAS = new IDelta[0];
	private static final int VERSION = 0x04;
	
	private Map compatibleChanges;
	private Map breakingChanges;
	
	public static BuildState read(DataInputStream in) throws IOException {
		String pluginID= in.readUTF();
		if (!pluginID.equals(ApiPlugin.PLUGIN_ID))
			throw new IOException(BuilderMessages.build_wrongFileFormat); 
		String kind= in.readUTF();
		if (!kind.equals("STATE")) //$NON-NLS-1$
			throw new IOException(BuilderMessages.build_wrongFileFormat); 
		if (in.readInt() != VERSION) {
			// this is an old build state - a full build is required
			return null;
		}
		if (in.readBoolean()) {
			// continue to read
			BuildState state = new BuildState();
			int numberOfCompatibleDeltas = in.readInt();
			// read all compatible deltas
			for (int i = 0; i < numberOfCompatibleDeltas; i++) {
				state.addCompatibleChange(readDelta(in));
			}
			int numberOfBreakingDeltas = in.readInt();
			// read all breaking deltas
			for (int i = 0; i < numberOfBreakingDeltas; i++) {
				state.addBreakingChange(readDelta(in));
			}
			return state;
		}
		return null;
	}
	public static void write(BuildState state, DataOutputStream out) throws IOException {
		out.writeUTF(ApiPlugin.PLUGIN_ID);
		out.writeUTF("STATE"); //$NON-NLS-1$
		out.writeInt(VERSION);
		out.writeBoolean(true);
		IDelta[] compatibleChangesDeltas = state.getCompatibleChanges();
		int length = compatibleChangesDeltas.length;
		out.writeInt(length);
		for (int i = 0; i < length; i++) {
			writeDelta(compatibleChangesDeltas[i], out);
		}
		IDelta[] breakingChangesDeltas = state.getBreakingChanges();
		int length2 = breakingChangesDeltas.length;
		out.writeInt(length2);
		for (int i = 0; i < length2; i++) {
			writeDelta(breakingChangesDeltas[i], out);
		}
	}
	private static IDelta readDelta(DataInputStream in) throws IOException {
		// decode the delta from the build state
		boolean hasComponentID = in.readBoolean();
		String componentID = null;
		if (hasComponentID) in.readUTF(); // delta.getComponentID()
		int elementType = in.readInt(); // delta.getElementType()
		int kind = in.readInt(); // delta.getKind()
		int flags = in.readInt(); // delta.getFlags()
		int restrictions = in.readInt(); // delta.getRestrictions()
		int modifiers = in.readInt(); // delta.getModifiers()
		String typeName = in.readUTF(); // delta.getTypeName()
		String key = in.readUTF(); // delta.getKey()
		int length = in.readInt(); // arguments.length;
		String[] datas = null;
		if (length != 0) {
			ArrayList arguments = new ArrayList();
			for (int i = 0; i < length; i++) {
				arguments.add(in.readUTF());
			}
			datas = new String[length];
			arguments.toArray(datas);
		} else {
			datas = new String[1];
			datas[0] = typeName.replace('$', '.');
		}
		return new Delta(componentID, elementType, kind, flags, restrictions, modifiers, typeName, key, datas);
	}
	private static void writeDelta(IDelta delta, DataOutputStream out) throws IOException {
		// encode a delta into the build state
		// int elementType, int kind, int flags, int restrictions, int modifiers, String typeName, String key, Object data
		String apiComponentID = delta.getApiComponentID();
		boolean hasComponentID = apiComponentID != null;
		out.writeBoolean(hasComponentID);
		if (hasComponentID) {
			out.writeUTF(apiComponentID);
		}
		out.writeInt(delta.getElementType());
		out.writeInt(delta.getKind());
		out.writeInt(delta.getFlags());
		out.writeInt(delta.getRestrictions());
		out.writeInt(delta.getModifiers());
		out.writeUTF(delta.getTypeName());
		out.writeUTF(delta.getKey());
		String[] arguments = delta.getArguments();
		int length = arguments.length;
		out.writeInt(length);
		for (int i = 0; i < length; i++) {
			out.writeUTF(arguments[i]);
		}
	}

	BuildState() {
		this.compatibleChanges = new HashMap();
		this.breakingChanges = new HashMap();
	}
	public void addCompatibleChange(IDelta delta) {
		String typeName = delta.getTypeName();
		Set object = (Set) this.compatibleChanges.get(typeName);
		if (object == null) {
			Set changes = new HashSet();
			changes.add(delta);
			this.compatibleChanges.put(typeName, changes);
		} else {
			object.add(delta);
		}
	}

	public void addBreakingChange(IDelta delta) {
		String typeName = delta.getTypeName();
		Set object = (Set) this.breakingChanges.get(typeName);
		if (object == null) {
			Set changes = new HashSet();
			changes.add(delta);
			this.breakingChanges.put(typeName, changes);
		} else {
			object.add(delta);
		}
	}
	
	/**
	 * @return the complete list of recorded breaking changes with duplicates removed, or 
	 * an empty array, never <code>null</code>
	 */
	public IDelta[] getBreakingChanges() {
		if (this.breakingChanges == null || this.breakingChanges.size() == 0) {
			return EMPTY_DELTAS;
		}
		HashSet collector = new HashSet();
		Collection values = this.breakingChanges.values();
		for (Iterator iterator = values.iterator(); iterator.hasNext(); ) {
			collector.addAll((HashSet) iterator.next());
		}
		return (IDelta[]) collector.toArray(new IDelta[collector.size()]);
	}

	/**
	 * @return the complete list of recorded compatible changes with duplicates removed,
	 * or an empty array, never <code>null</code>
	 */
	public IDelta[] getCompatibleChanges() {
		if (this.compatibleChanges == null || this.compatibleChanges.size() == 0) {
			return EMPTY_DELTAS;
		}
		HashSet collector = new HashSet();
		Collection values = this.compatibleChanges.values();
		for (Iterator iterator = values.iterator(); iterator.hasNext(); ) {
			collector.addAll((HashSet) iterator.next());
		}
		return (IDelta[]) collector.toArray(new IDelta[collector.size()]);
	}

	/**
	 * Remove all entries for the given type name.
	 *
	 * @param typeName the given type name
	 */
	public void cleanup(String typeName) {
		this.breakingChanges.remove(typeName);
		this.compatibleChanges.remove(typeName);
	}
}
