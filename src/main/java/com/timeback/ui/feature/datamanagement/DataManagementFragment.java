package com.timeback.ui.feature.datamanagement;

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
 * UI-08 데이터 관리 화면.
 *
 * 처리하는 ScreenState:
 * - Loading: 데이터 관리 상태 로딩 중
 * - Content(DataManagementViewData): 백업/보존/삭제 상태 표시
 * - Error: 로딩 실패
 *
 * 사용자 액션:
 * - 보존 기간 변경
 * - 전체 삭제 요청 및 확인
 * - 삭제 진행 상태 확인
 */
@AndroidEntryPoint
public class DataManagementFragment extends Fragment {

    private DataManagementViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DataManagementViewModel.class);
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
                // TODO: DataManagementViewData로 렌더링
                //   - 백업 상태 카드 (마지막 성공 시각, 대기 건수)
                //   - 보존 기간 선택
                //   - 전체 삭제 버튼 (확인 다이얼로그)
                //   - 삭제 진행 상태 (device/server 완료 여부)
            } else if (state instanceof ScreenState.Error) {
                // TODO: 에러 + 재시도
            }
        });

        viewModel.actionResult.observe(getViewLifecycleOwner(), result -> {
            // TODO: 보존 변경/삭제 결과 피드백
        });
    }
}
