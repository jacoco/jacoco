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

public class ClientInfo {
	private long id;
	private int type;
	
	public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
    public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public ClientInfo(long id, int type) {
		this.id = id;
		this.type = type;
	}
	
	
}
