/*******************************************************************************
 * Copyright (c) 2009, 2010 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 * $Id: $
 *******************************************************************************/

(function () {

  /**
   * Sorts the columns with the given header.
   */  
  function sortColumn(header) {
    var table = header.parentNode.parentNode.parentNode
    var body = table.tBodies[0]
    var colidx = getNodePosition(header)
    var sortup = header.className.indexOf('down ') == 0
    
    resetSortedStyle(table)
    
    var rows = body.rows
    var sortedrows = []
    for (var i = 0; i < rows.length; i++) {
      r = rows[i]
      sortedrows[parseInt(r.childNodes[colidx].id.slice(1))] = r
    }
    
    if (sortup) {
      for (var i = sortedrows.length - 1; i >= 0; i--) {
        body.appendChild(sortedrows[i])
      }
      header.className = 'up ' + header.className
    } else {
      for (var i = 0; i < sortedrows.length; i++) {
        body.appendChild(sortedrows[i])
      }
      header.className = 'down ' + header.className
    }
  }

  /**
   * Calculates the position of a element within its parent.
   */  
  function getNodePosition(element) {
    var pos = -1;
    while (element) {
      element = element.previousSibling
      pos++
    }
    return pos
  }

  /**
   * Remove the sorting indicator style from all headers.
   */
  function resetSortedStyle(table) {
    for (var c = table.tHead.firstChild.firstChild; c; c = c.nextSibling) {
      if (c.className) {
        if (c.className.indexOf('down ') == 0) {
          c.className = c.className.slice(5)
        }
        if (c.className.indexOf('up ') == 0) {
          c.className = c.className.slice(3)
        }
      }
    }
  }
  
  window['sortColumn'] = sortColumn

})();