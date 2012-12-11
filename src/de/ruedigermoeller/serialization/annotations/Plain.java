package de.ruedigermoeller.serialization.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Copyright (c) 2012, Ruediger Moeller. All rights reserved.
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 * <p/>
 * Date: 11.12.12
 * Time: 21:16
 *
 */

/**
 * applicable to int array fields. by default FST applies a moderate compression for integers. However if you submit int arrays
 * covering the full integer range (e.g. image/sound data), this might actually increase the size of the int array. Use this
 * to let FST write an int array 'plain' (4 bytes per int)
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Plain {
}
