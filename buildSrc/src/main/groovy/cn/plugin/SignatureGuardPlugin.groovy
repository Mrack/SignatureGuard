package cn.plugin


import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @author Mrack
 * @date 2021/1/31
 */

class SignatureGuardPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.extensions.create("signatureGuard", SignatureConfig)
        def android = project.extensions.android
        android.registerTransform(new SignatureGuardTransform(project))

    }

}
