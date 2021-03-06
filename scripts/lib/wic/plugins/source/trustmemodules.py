# Copyright (c) 2014, Intel Corporation.
# All rights reserved.
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License version 2 as
# published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
#
# DESCRIPTION
# This implements the 'bootimg-efi' source plugin class for 'wic'
#
# AUTHORS
# Tom Zanussi <tom.zanussi (at] linux.intel.com>
#
#
# This file was adapted from it's original version distributed with poky
# Copyright(c) 2018 Fraunhofer AISEC
# Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
#
# Contact Information:
# Fraunhofer AISEC <trustme@aisec.fraunhofer.de>
#

import logging
import os
import shutil

from wic import WicError
from wic.engine import get_custom_config
from wic.pluginbase import SourcePlugin
from wic.misc import (exec_cmd, exec_native_cmd,
                      get_bitbake_var, BOOTDD_EXTRA_SPACE)

logger = logging.getLogger('wic')

class TrustmeModulesPlugin(SourcePlugin):
    """
    Creates trustme kernel modules partition
    Based on bootimg-efi.py
    """

    name = 'trustmemodules'


    @classmethod
    def do_configure_partition(cls, part, source_params, creator, cr_workdir,
                               oe_builddir, bootimg_dir, kernel_dir,
                               native_sysroot):
        hdddir = "%s/hdd/modules" % cr_workdir

        install_cmd = "install -d %s" % hdddir
        exec_cmd(install_cmd)


    @classmethod
    def do_prepare_partition(cls, part, source_params, creator, cr_workdir,
                             oe_builddir, bootimg_dir, kernel_dir,
                             rootfs_dir, native_sysroot):
        if not kernel_dir:
            kernel_dir = get_bitbake_var("DEPLOY_DIR_IMAGE")
            if not kernel_dir:
                raise WicError("Couldn't find DEPLOY_DIR_IMAGE, exiting")

        hdddir = "%s/hdd/" % cr_workdir

        machine_translated = get_bitbake_var('MACHINE_ARCH')

        #underscores in MACHINE_ARCH are replaced by - in filenames
        machine_translated = machine_translated.replace("_","-")

        kernel_stagingdir = get_bitbake_var("STAGING_KERNEL_BUILDDIR")

        rootfs = get_bitbake_var("IMAGE_ROOTFS")

        versionfile = open(kernel_stagingdir + "/kernel-abiversion", "r")
        kernelversion = versionfile.read().rstrip()
        versionfile.close()
        
        modulesname = "{0}/modules-{1}.tgz".format(kernel_dir, machine_translated)
        modulesname = os.readlink(modulesname)

        try:
            cp_cmd = "tar -xzf {0}/{1} --directory {2}".format(kernel_dir, modulesname, hdddir)
            exec_cmd(cp_cmd, True)
        except KeyError:
            raise WicError("error while copying kernel modules")

        try:
            cp_cmd = "/sbin/depmod --basedir \"{1}\" --config \"{0}/etc/depmod.d\" {2}".format(rootfs, hdddir, kernelversion)
            exec_cmd(cp_cmd, True)
        except KeyError:
            raise WicError("Failed to execute depmod on modules")
        
        du_cmd = "du -B 1 -s %s" % hdddir
        out = exec_cmd(du_cmd)
        size_bytes = int(out.split()[0])

        size_bytes += 2**20

        logger.debug("out: %s, final size: %d", out, size_bytes)

        # create filesystem image 
        modulesimg = "%s/modules.img" % cr_workdir

        dosfs_cmd = "mksquashfs \"{0}/lib/modules/{1}\" {2} -b {3} -noI -noD -noF -noX -all-root  ".format(hdddir, kernelversion, modulesimg, "4096")
        logger.debug("Executing: %s" % dosfs_cmd)
        exec_native_cmd(dosfs_cmd, native_sysroot)

        chmod_cmd = "chmod 644 %s" % modulesimg
        exec_cmd(chmod_cmd)

        du_cmd = "du -Lbks %s" % modulesimg
        out = exec_cmd(du_cmd)
        modulesimg_size = out.split()[0]

        part.size = int(modulesimg_size)
        part.source_file = modulesimg
