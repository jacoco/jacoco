/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.core.data;

public class SocketClientData implements IClientInfoVisitor {

	private ClientInfo info = new ClientInfo(0);
	
	public ClientInfo getInfo() {
		return info;
	}
	public void setInfo(ClientInfo info) {
		this.info = info;
	}
	@Override
	public void visitClientInfo(ClientInfo info) {
		this.info = info;
	}

}
