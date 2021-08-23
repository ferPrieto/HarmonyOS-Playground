package com.fprieto.wearable;

import com.fprieto.wearable.presentation.ui.slice.MainAbilitySlice;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import java.util.ArrayList;
import java.util.List;

public class HiWearMainAbility extends Ability {

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setMainRoute(MainAbilitySlice.class.getName());

        setSwipeToDismiss(true);
        final String[] permission = {
                "ohos.permission.MICROPHONE",
                "ohos.permission.WRITE_USER_STORAGE",
                "ohos.permission.LOCATION",
                "ohos.permission.LOCATION_IN_BACKGROUND",
                "ohos.permission.ACTIVITY_MOTION",
                "ohos.permission.READ_HEALTH_DATA"
        };

        final List<String> permissionList = new ArrayList<>();
        for (String s : permission) {
            if (verifySelfPermission(s) != 0 && canRequestPermission(s)) {
                permissionList.add(s);
            }
        }
        if (permissionList.size() > 0) {
            requestPermissionsFromUser(permissionList.toArray(new String[0]), 0);
        }
    }
}
