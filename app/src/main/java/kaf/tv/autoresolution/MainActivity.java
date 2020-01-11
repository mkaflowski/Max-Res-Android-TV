/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package kaf.tv.autoresolution;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;
import androidx.leanback.widget.GuidedActionsStylist;

import com.socks.library.KLog;

import java.util.ArrayList;
import java.util.List;

/*
 * Main Activity class that loads {@link MainFragment}.
 */
public class MainActivity extends FragmentActivity {

    private static final int BACK = 2;

    private static final int FIRST_NAME = 3;
    private static final int LAST_NAME = 4;
    private static final int PASSWORD = 5;
    private static final int PAYMENT = 6;

    private static final int OPTION_CHECK_SET_ID = 10;
    private static final int DEFAULT_OPTION = 0;
    private static final String[] OPTION_NAMES = {"Option A", "Option B", "Option C"};
    private static final String[] OPTION_DESCRIPTIONS = {"Here's one thing you can do",
            "Here's another thing you can do", "Here's one more thing you can do"};

    private static final String TAG = "Tag";
    private BootReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        KLog.d();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guided_step_activity);
        GuidedStepSupportFragment.addAsRoot(this, new FirstStepFragment(), R.id.lb_guidedstep_host);
//        getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefFragment()).commit();

        IntentFilter filter = new IntentFilter();
        filter.addAction(OverlayService.RES_CHANGED_BROADCAST);
        receiver = new BootReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                super.onReceive(context, intent);
                Handler h = new Handler();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        KLog.e();
                        recreate();
                    }
                }, 2000);
            }
        };
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.v(TAG, "onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.v(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.v(TAG, "onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
    }

    private static void addAction(List<GuidedAction> actions, long id, String title, String desc) {
        actions.add(new GuidedAction.Builder()
                .id(id)
                .title(title)
                .description(desc)
                .build());
    }

    private static void addEditableAction(List<GuidedAction> actions, long id, String title, String desc) {
        actions.add(new GuidedAction.Builder()
                .id(id)
                .title(title)
                .description(desc)
                .editable(true)
                .build());
    }

    private static void addEditableAction(List<GuidedAction> actions, long id, String title,
                                          String editTitle, String desc) {
        actions.add(new GuidedAction.Builder()
                .id(id)
                .title(title)
                .editTitle(editTitle)
                .description(desc)
                .editable(true)
                .build());
    }

    private static void addEditableAction(List<GuidedAction> actions, long id, String title,
                                          String editTitle, int editInputType, String desc, String editDesc) {
        actions.add(new GuidedAction.Builder()
                .id(id)
                .title(title)
                .editTitle(editTitle)
                .editInputType(editInputType)
                .description(desc)
                .editDescription(editDesc)
                .editable(true)
                .build());
    }

    private static void addEditableDescriptionAction(List<GuidedAction> actions, long id,
                                                     String title, String desc, String editDescription, int descriptionEditInputType) {
        actions.add(new GuidedAction.Builder()
                .id(id)
                .title(title)
                .description(desc)
                .editDescription(editDescription)
                .descriptionEditInputType(descriptionEditInputType)
                .descriptionEditable(true)
                .build());
    }

    private static void addCheckedAction(List<GuidedAction> actions, Context context,
                                         String title, String desc, int checkSetId) {
        actions.add(new GuidedAction.Builder()
                .title(title)
                .description(desc)
                .checkSetId(checkSetId)
                .build());
    }

    public static class FirstStepFragment extends GuidedStepSupportFragment {

        private int ACTION_OPEN_SETTINGS = 1;
        private int ACTION_ON = 2;
        private int ACTION_OFF = 3;
        private int ACTION_ON_DEBUG = 4;
        private int ACTION_OFF_DEBUG = 5;

        @Override
        public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
            String title = getString(R.string.app_name);
//            String breadcrumb = getString(R.string.guidedstep_first_breadcrumb);
//            String description = getString(R.string.guidedstep_first_description);
            Drawable icon = getActivity().getDrawable(R.drawable.app_icon_your_company);

            return new GuidanceStylist.Guidance(title, "", "", icon);
        }


        @Override
        public void onCreateActions(List<GuidedAction> actions, Bundle savedInstanceState) {
            boolean canWriteOverlay = OverlayService.canWriteOverlay(getActivity());
            if (!canWriteOverlay)
                actions.add(new GuidedAction.Builder(getContext())
                        .title(getActivity().getString(R.string.check_diaplsy) + getActivity().getString(R.string.app_name))
                        .description(getActivity().getString(R.string.check_desc))
                        .id(ACTION_OPEN_SETTINGS)
                        .build());

            WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();

            GuidedAction action = new GuidedAction.Builder(getContext())
                    .title(getActivity().getString(R.string.set_max))
                    .description(getActivity().getString(R.string.current) + display.getMode())
                    .id(GuidedAction.ACTION_ID_CONTINUE)
                    .build();
            action.setEnabled(canWriteOverlay);
            actions.add(action);

            KLog.i(OverlayService.getAutoSetPref(getActivity()));

            GuidedAction on = new GuidedAction.Builder(getContext())
                    .title(getActivity().getString(R.string.on))
                    .id(ACTION_ON)
                    .checkSetId(1)
                    .checked(OverlayService.getAutoSetPref(getActivity()))
                    .build();
            GuidedAction off = new GuidedAction.Builder(getContext())
                    .title(getActivity().getString(R.string.off))
                    .checkSetId(1)
                    .checked(!OverlayService.getAutoSetPref(getActivity()))
                    .id(ACTION_OFF)
                    .build();

            List<GuidedAction> list = new ArrayList<>();
            list.add(on);
            list.add(off);
            GuidedAction turnOnAutomatically = new GuidedAction.Builder(getContext())
                    .title(getActivity().getString(R.string.turn_auto))
                    .enabled(canWriteOverlay)
                    .subActions(list)
                    .build();
            actions.add(turnOnAutomatically);


            GuidedAction ond = new GuidedAction.Builder(getContext())
                    .title(getActivity().getString(R.string.on))
                    .id(ACTION_ON_DEBUG)
                    .checkSetId(2)
                    .checked(OverlayService.getShowDebug(getActivity()))
                    .build();
            GuidedAction offd = new GuidedAction.Builder(getContext())
                    .title(getActivity().getString(R.string.off))
                    .checkSetId(2)
                    .checked(!OverlayService.getShowDebug(getActivity()))
                    .id(ACTION_OFF_DEBUG)
                    .build();

            list = new ArrayList<>();
            list.add(ond);
            list.add(offd);
            turnOnAutomatically = new GuidedAction.Builder(getContext())
                    .title(getActivity().getString(R.string.debug))
                    .enabled(canWriteOverlay)
                    .subActions(list)
                    .description(getActivity().getString(R.string.debug_desc))
                    .build();
            actions.add(turnOnAutomatically);

            actions.add(new GuidedAction.Builder(getContext())
                    .title(getActivity().getString(R.string.about))
                    .description(getActivity().getString(R.string.created))
                    .infoOnly(true)
                    .build());
        }

        @Override
        public void onGuidedActionClicked(GuidedAction action) {
            KLog.i(action.getId());
            if (action.getId() == GuidedAction.ACTION_ID_CONTINUE) {
                OverlayService.actionAddOverlay(getActivity());
            } else if (action.getId() == ACTION_OPEN_SETTINGS) {
                if (OverlayService.canWriteOverlay(getActivity())) {
                    getActivity().recreate();
                    return;
                }
                KLog.d();
                if (android.os.Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(getActivity())) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getActivity().getPackageName()));
                    try {
                        getActivity().startActivityFromFragment(this, intent, 103);
                    } catch (Exception e) {
                        intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getActivity().startActivityFromFragment(this, intent, 103);
                    }
                } else {
                    Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getActivity().startActivityFromFragment(this, intent, 103);
                }

            }
        }

        @Override
        public boolean onSubGuidedActionClicked(GuidedAction action) {
            if (action.getId() == ACTION_ON) {
                KLog.i(OverlayService.getAutoSetPref(getActivity()));
                OverlayService.setAutoSetPref(getActivity(), true);
            } else if (action.getId() == ACTION_OFF) {
                KLog.i(OverlayService.getAutoSetPref(getActivity()));
                OverlayService.setAutoSetPref(getActivity(), false);
            } else if (action.getId() == ACTION_ON_DEBUG) {
                KLog.i(OverlayService.getShowDebug(getActivity()));
                OverlayService.setShowDebug(getActivity(), true);
            } else if (action.getId() == ACTION_OFF_DEBUG) {
                KLog.i(OverlayService.getShowDebug(getActivity()));
                OverlayService.setShowDebug(getActivity(), false);
            }
            return true;
        }
    }

    public static class PrefFragment extends PreferenceFragment {

        private ListPreference mListPreference;


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.app_preferences);
        }

//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//
//            mListPreference = (ListPreference)  getPreferenceManager().findPreference("preference_key");
//            mListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//                @Override
//                public boolean onPreferenceChange(Preference preference, Object newValue) {
//                    // your code here
//                }
//            }
//
//            return inflater.inflate(R.layout.fragment_settings, container, false);
//        }
    }

    public static class SecondStepFragment extends GuidedStepSupportFragment {

        public GuidedActionsStylist onCreateActionsStylist() {
            return new GuidedActionsStylist() {
                protected void setupImeOptions(GuidedActionsStylist.ViewHolder vh,
                                               GuidedAction action) {
                    if (action.getId() == PASSWORD) {
                        vh.getEditableDescriptionView().setImeActionLabel("Confirm!",
                                EditorInfo.IME_ACTION_DONE);
                    } else {
                        super.setupImeOptions(vh, action);
                    }
                }
            };
        }

        @Override
        public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
            String title = getString(R.string.guidedstep_second_title);
            String breadcrumb = getString(R.string.guidedstep_second_breadcrumb);
            String description = getString(R.string.guidedstep_second_description);
            Drawable icon = getActivity().getDrawable(R.drawable.app_icon_your_company);
            return new GuidanceStylist.Guidance(title, description, breadcrumb, icon);
        }

        @Override
        public void onCreateActions(List<GuidedAction> actions, Bundle savedInstanceState) {
            addEditableAction(actions, FIRST_NAME, "Pat", "Your first name");
            addEditableAction(actions, LAST_NAME, "Smith", "Your last name");
            addEditableAction(actions, PAYMENT, "Payment", "", InputType.TYPE_CLASS_NUMBER,
                    "Input credit card number", "Input credit card number");
            addEditableDescriptionAction(actions, PASSWORD, "Password", "", "",
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }

        @Override
        public void onCreateButtonActions(List<GuidedAction> actions, Bundle savedInstanceState) {
            actions.add(new GuidedAction.Builder(getContext())
                    .description("Continue")
                    .build());
            actions.get(actions.size() - 1).setEnabled(false);
        }

        @Override
        public void onGuidedActionClicked(GuidedAction action) {
            if (action.getId() == GuidedAction.ACTION_ID_CONTINUE) {
                FragmentManager fm = getFragmentManager();
                GuidedStepSupportFragment.add(fm, new ThirdStepFragment(), R.id.lb_guidedstep_host);
            }
        }

        @Override
        public long onGuidedActionEditedAndProceed(GuidedAction action) {
            if (action.getId() == PAYMENT) {
                CharSequence editTitle = action.getEditTitle();
                if (TextUtils.isDigitsOnly(editTitle) && editTitle.length() == 16) {
                    editTitle = editTitle.subSequence(editTitle.length() - 4, editTitle.length());
                    action.setDescription("Visa XXXX-XXXX-XXXX-" + editTitle);
                    updateContinue(isPasswordValid());
                    return GuidedAction.ACTION_ID_NEXT;
                } else if (editTitle.length() == 0) {
                    action.setDescription("Input credit card number");
                    updateContinue(false);
                    return GuidedAction.ACTION_ID_CURRENT;
                } else {
                    action.setDescription("Error credit card number");
                    updateContinue(false);
                    return GuidedAction.ACTION_ID_CURRENT;
                }
            } else if (action.getId() == PASSWORD) {
                CharSequence password = action.getEditDescription();
                if (password.length() > 0) {
                    if (isPaymentValid()) {
                        updateContinue(true);
                        return GuidedAction.ACTION_ID_NEXT;
                    } else {
                        updateContinue(false);
                        return GuidedAction.ACTION_ID_CURRENT;
                    }
                } else {
                    updateContinue(false);
                    return GuidedAction.ACTION_ID_CURRENT;
                }
            }
            return GuidedAction.ACTION_ID_NEXT;
        }

        boolean isPaymentValid() {
            return findActionById(PAYMENT).getDescription().subSequence(0, 4).toString().equals("Visa");
        }

        boolean isPasswordValid() {
            return findActionById(PASSWORD).getEditDescription().length() > 0;
        }

        void updateContinue(boolean enabled) {
            findButtonActionById(GuidedAction.ACTION_ID_CONTINUE).setEnabled(enabled);
            notifyButtonActionChanged(findButtonActionPositionById(
                    GuidedAction.ACTION_ID_CONTINUE));
        }
    }

    public static class ThirdStepFragment extends GuidedStepSupportFragment {

        private int mSelectedOption = DEFAULT_OPTION;

        @Override
        public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
            String title = getString(R.string.guidedstep_third_title);
            String breadcrumb = getString(R.string.guidedstep_third_breadcrumb);
            String description = getString(R.string.guidedstep_third_description);
            Drawable icon = getActivity().getDrawable(R.drawable.app_icon_your_company);
            return new GuidanceStylist.Guidance(title, description, breadcrumb, icon);
        }

        @Override
        public GuidanceStylist onCreateGuidanceStylist() {
            return new GuidanceStylist() {
                @Override
                public int onProvideLayoutId() {
                    return R.layout.guidedstep_second_guidance;
                }
            };
        }

        @Override
        public void onCreateActions(List<GuidedAction> actions, Bundle savedInstanceState) {
            String desc = "The description can be quite long as well.  ";
            desc += "Just be sure to set multilineDescription to true in the GuidedAction.";
            actions.add(new GuidedAction.Builder()
                    .title("Note that Guided Actions can have titles that are quite long.")
                    .description(desc)
                    .multilineDescription(true)
                    .infoOnly(true)
                    .enabled(true)
                    .focusable(false)
                    .build());
            for (int i = 0; i < OPTION_NAMES.length; i++) {
                addCheckedAction(actions, getActivity(), OPTION_NAMES[i],
                        OPTION_DESCRIPTIONS[i], GuidedAction.DEFAULT_CHECK_SET_ID);
                if (i == DEFAULT_OPTION) {
                    actions.get(actions.size() - 1).setChecked(true);
                }
            }
            for (int i = 0; i < OPTION_NAMES.length; i++) {
                addCheckedAction(actions, getActivity(), OPTION_NAMES[i],
                        OPTION_DESCRIPTIONS[i], GuidedAction.CHECKBOX_CHECK_SET_ID);
            }
        }

        @Override
        public void onCreateButtonActions(List<GuidedAction> actions, Bundle savedInstanceState) {
            actions.add(new GuidedAction.Builder(getActivity())
                    .build());
        }

        @Override
        public void onGuidedActionClicked(GuidedAction action) {
            if (action.getId() == GuidedAction.ACTION_ID_CONTINUE) {
                FragmentManager fm = getFragmentManager();
                FourthStepFragment f = new FourthStepFragment();
                Bundle arguments = new Bundle();
                arguments.putInt(FourthStepFragment.EXTRA_OPTION, mSelectedOption);
                f.setArguments(arguments);
                GuidedStepSupportFragment.add(fm, f, R.id.lb_guidedstep_host);
            } else if (action.getCheckSetId() == GuidedAction.DEFAULT_CHECK_SET_ID) {
                mSelectedOption = getSelectedActionPosition() - 1;
            }
        }

    }

    public static class FourthStepFragment extends GuidedStepSupportFragment {
        public static final String EXTRA_OPTION = "extra_option";

        public FourthStepFragment() {
        }

        public int getOption() {
            Bundle b = getArguments();
            if (b == null) return 0;
            return b.getInt(EXTRA_OPTION, 0);
        }

        @Override
        public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
            String title = getString(R.string.guidedstep_fourth_title);
            String breadcrumb = getString(R.string.guidedstep_fourth_breadcrumb);
            String description = "You chose: " + OPTION_NAMES[getOption()];
            Drawable icon = getActivity().getDrawable(R.drawable.app_icon_your_company);
            return new GuidanceStylist.Guidance(title, description, breadcrumb, icon);
        }

        @Override
        public void onCreateActions(List<GuidedAction> actions, Bundle savedInstanceState) {
            actions.add(new GuidedAction.Builder(getActivity())
                    .description("All Done...")
                    .build());
            addAction(actions, BACK, "Start Over", "Let's try this again...");
        }

        @Override
        public void onGuidedActionClicked(GuidedAction action) {
            if (action.getId() == GuidedAction.ACTION_ID_FINISH) {
                finishGuidedStepSupportFragments();
            } else if (action.getId() == BACK) {
                // pop 4, 3, 2
                popBackStackToGuidedStepSupportFragment(SecondStepFragment.class,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        }

    }


}
