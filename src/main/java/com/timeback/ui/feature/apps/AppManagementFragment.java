package com.timeback.ui.feature.apps;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.timeback.R;

import com.timeback.ui.domain.model.ScreenState;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * UI-04 앱 관리 화면.
 *
 * 처리하는 ScreenState:
 * - Loading: 앱 목록 로딩 중
 * - Content(AppManagementViewData): 설치된 앱 목록 + 분류 상태 표시
 * - Error: 앱 목록 로딩 실패
 *
 * 사용자 액션:
 * - 앱 분류 변경 (PRODUCTIVE, LEISURE, WASTE, NEUTRAL)
 */
@AndroidEntryPoint
public class AppManagementFragment extends Fragment {

    private AppManagementViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AppManagementViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_placeholder, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        observeState();
    }

    private void observeState() {
        viewModel.state.observe(getViewLifecycleOwner(), state -> {
            if (state instanceof ScreenState.Loading) {
                // TODO: 로딩 표시
            } else if (state instanceof ScreenState.Content) {
                // TODO: AppManagementViewData로 앱 리스트 렌더링
                //   - 앱 아이콘 + 이름
                //   - 현재 분류 칩
                //   - 분류 변경 드롭다운
            } else if (state instanceof ScreenState.Error) {
                // TODO: 에러 + 재시도
            }
        });

        viewModel.actionResult.observe(getViewLifecycleOwner(), result -> {
            // TODO: 분류 변경 결과 피드백
        });
    }
}
